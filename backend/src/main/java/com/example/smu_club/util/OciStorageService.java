package com.example.smu_club.util;

import com.example.smu_club.exception.custom.OciUploadException;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
                        .name("Upload-PAR-" + uniqueFileName.substring(0, 10)) // PAR의 이름 (관리 용도)
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

   /* public String upload(MultipartFile file) {
        try {
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();


            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest request = PutObjectRequest.builder()
                        .namespaceName(namespace)
                        .bucketName(bucketName)
                        .objectName(uniqueFileName)
                        .putObjectBody(inputStream)
                        .contentType(file.getContentType())
                        .build();

                client.putObject(request);
            }


            String encodedFileName = URLEncoder.encode(uniqueFileName, StandardCharsets.UTF_8);

            return String.format(
                    "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                    region,
                    namespace,
                    bucketName,
                    encodedFileName
            );

        } catch (Exception e) {
            log.error("OCI file upload failed. Cause: {}", e.getMessage(), e);
            throw new OciUploadException("파일 업로드 중 서버 오류가 발생했습니다.");
        }
    }*/
}
