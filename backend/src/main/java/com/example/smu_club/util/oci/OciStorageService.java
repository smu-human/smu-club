package com.example.smu_club.util.oci;

import com.example.smu_club.exception.custom.OciDeletionException;
import com.example.smu_club.exception.custom.OciSearchException;
import com.example.smu_club.exception.custom.OciUploadException;
import com.example.smu_club.util.PreSignedUrlResponse;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class OciStorageService {

    private final ObjectStorageClient client;
    private final String namespace;
    private final String bucketName;
    private final String region;



    public OciStorageService(
            @Value("${oci.config.tenancy-id}") String tenancyId,
            @Value("${oci.config.user-id}") String userId,
            @Value("${oci.config.fingerprint}") String fingerprint,
            @Value("${oci.config.private-key-path}") String privateKeyPath,
            @Value("${oci.config.region}") String region,
            @Value("${oci.bucket.namespace}") String namespace,
            @Value("${oci.bucket.name}") String bucketName
    ) {


        Supplier<InputStream> privateKeySupplier = () -> {
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(privateKeyPath);
                if (inputStream == null) {
                    throw new IOException("Private key file not found at: " + privateKeyPath);
                }
                return inputStream;
            } catch (IOException e) {
                log.error("OCI Private Key 파일 읽기 실패. Cause: {}", e.getMessage(), e);
                throw new OciUploadException("OCI Private Key 파일을 읽는데 실패했습니다. (서버 시작 불가)");
            }
        };

        AuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenancyId)
                .userId(userId)
                .fingerprint(fingerprint)
                .privateKeySupplier(privateKeySupplier)
                .build();

        this.client = ObjectStorageClient.builder()
                .region(Region.fromRegionId(region))
                .build(provider);

        this.namespace = namespace;
        this.bucketName = bucketName;
        this.region = region;
    }


    public PreSignedUrlResponse createUploadPreSignedUrl(String originalFileName, String contentType) {

        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;

        Date expirationDate = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

        CreatePreauthenticatedRequestDetails details =
                CreatePreauthenticatedRequestDetails.builder()
                        .name("Upload-PAR-" + uniqueFileName.substring(0, 10)) // PAR(Pre Authenticated Request)의 이름 (관리 용도)
                        .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite) // 접근 유형: 객체 쓰기 (PUT 요청 허용)
                        .objectName(uniqueFileName) // PAR이 적용될 객체 이름
                        .timeExpires(expirationDate) // 만료 시간
                        .build();

        CreatePreauthenticatedRequestRequest request =
                CreatePreauthenticatedRequestRequest.builder()
                        .namespaceName(namespace)
                        .bucketName(bucketName)
                        .createPreauthenticatedRequestDetails(details)
                        .build();

        try {

            CreatePreauthenticatedRequestResponse response = client.createPreauthenticatedRequest(request);

            String accessUrl = response.getPreauthenticatedRequest().getAccessUri();

            String parUrl = String.format(
                    "https://objectstorage.%s.oraclecloud.com%s",
                    region,
                    accessUrl
            );

            return new PreSignedUrlResponse(uniqueFileName, parUrl);
        } catch (Exception e) {
            log.error("OCI Pre-Signed URL creation failed. Cause: {}", e.getMessage(), e);
            throw new OciUploadException("파일 업로드 URL 생성 중 서버 오류가 발생했습니다.");
        }
    }

    public String createFinalOciUrl(String uniqueFileName) {
        try {
            String encodedFileName = URLEncoder.encode(uniqueFileName, StandardCharsets.UTF_8);

            return String.format(
                    "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                    region,
                    namespace,
                    bucketName,
                    encodedFileName
            );
        } catch (Exception e) {
            log.error("OCI Final-Oci URL creation failed. Cause: {}", e.getMessage(), e);
            throw new OciUploadException("OCI 최종 URL 생성 중 오류가 발생했습니다.");
        }
    }

    public void deleteUrls(List<String> urls) {
        int ssuccess = 0;
        int fail = 0;
        for(String url : urls){
            try{
                String objectName = extractObjectNameFromUrl(url);
                deleteObject(objectName);
                ssuccess++;

            } catch(Exception e){
                log.error("OCI URL 삭제 중 오류 발생(건너 뜀): url = {}, cause = {}", url, e.getMessage());
                fail++;
            }
        }
        log.info("OCI URL 삭제 완료: 성공 {}건, 실패 {}건", ssuccess, fail);
    }

    private String extractObjectNameFromUrl(String fullUrl) {
        final String OBJECT_PATH_DELIMITER = "/o/";
        int startIndex = fullUrl.indexOf(OBJECT_PATH_DELIMITER);

        if (startIndex == -1) {
            log.error("Invalid OCI URL format for deletion: {}", fullUrl);
            throw new IllegalArgumentException("OCI URL 형식이 잘못되었습니다: /o/ 구분이 없습니다.");
        }

        String encodedFileName = fullUrl.substring(startIndex + OBJECT_PATH_DELIMITER.length());

        try {
            return URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error("URL Decode failed for: {}", encodedFileName, e);
            throw new IllegalArgumentException("OCI 파일명 디코딩 실패: " + encodedFileName);
        }
    }

    @Retryable(
            value = {OciDeletionException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    private void deleteObject(String objectName) throws OciDeletionException {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(objectName)
                .build();

        try {
            client.deleteObject(request);
            log.info("OCI object deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("OCI object deletion failed for {}. Cause: {}", objectName, e.getMessage(), e);
            throw new OciDeletionException("OCI 파일 삭제 요청 실패 " + objectName);
        }
    }

    public List<String> getOldFileKeys(int hours) {
        List<String> oldFiles = new ArrayList<>();


        //현재 기준시간 - hours 이전 시간
        Date timeThreshold = Date.from(Instant.now().minus(hours, ChronoUnit.HOURS));

        String nextToken = null; // 페이지네이션용 토큰

        try{
            //파일 수가 많을 걸 대비하여 do-while로 모든 페이지 조회
            do{
                ListObjectsRequest request = ListObjectsRequest.builder()
                        .namespaceName(namespace)
                        .bucketName(bucketName)
                        .fields("name,timeCreated") //필요한 필드만 조회 (최적화)
                        .start(nextToken)
                        .build();

                ListObjectsResponse response = client.listObjects(request);

                for(var obj : response.getListObjects().getObjects()) {
                    if(obj.getTimeCreated().before(timeThreshold)) { //임계값 이전 파일이면
                        oldFiles.add(obj.getName()); //파일명 추가
                    }
                }
                //다음 페이지 토큰 갱신
                nextToken = response.getListObjects().getNextStartWith();

            } while(nextToken != null);
        } catch(Exception e){
            log.error("OCI 오래된 파일 조회 실패. Cause: {}", e.getMessage(), e);
            throw new OciSearchException("OCI 오래된 파일 조회 중 서버 오류가 발생했습니다.");
        }

        return oldFiles;

    }

}
