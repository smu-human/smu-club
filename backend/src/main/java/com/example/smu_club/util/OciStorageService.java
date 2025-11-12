package com.example.smu_club.util; // 패키지 경로는 본인 프로젝트에 맞게 수정하세요

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class OciStorageService {

    private final ObjectStorageClient client;
    private final String namespace;
    private final String bucketName;
    private final String region;

    // 1. 생성자: application.properties에서 OCI 설정값들을 자동으로 주입받습니다.
    public OciStorageService(
            @Value("${oci.config.tenancy-id}") String tenancyId,
            @Value("${oci.config.user-id}") String userId,
            @Value("${oci.config.fingerprint}") String fingerprint,
            @Value("${oci.config.private-key-path}") String privateKeyPath,
            @Value("${oci.config.region}") String region,
            @Value("${oci.bucket.namespace}") String namespace,
            @Value("${oci.bucket.name}") String bucketName
    ) throws IOException {

        // .pem 파일 경로를 읽어오는 Supplier 생성
        Supplier<InputStream> privateKeySupplier = () -> {
            try {
                // 'src/main/resources'에 있는 파일은 ClassLoader를 통해 읽어야 합니다.
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                        // 'src/main/resources/'를 제외한 경로를 적어야 합니다.
                        privateKeyPath.replace("src/main/resources/", "")
                );
                if (inputStream == null) {
                    throw new IOException("Private key file not found at: " + privateKeyPath);
                }
                return inputStream;
            } catch (IOException e) {
                // Supplier 내에서 발생하는 체크 예외를 런타임 예외로 래핑합니다.
                throw new RuntimeException("Failed to read private key", e);
            }
        };

        // OCI 인증 객체(AuthenticationDetailsProvider) 생성
        AuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenancyId)
                .userId(userId)
                .fingerprint(fingerprint)
                .privateKeySupplier(privateKeySupplier)
                .build();

        // OCI ObjectStorage 클라이언트(통신 객체) 생성
        this.client = ObjectStorageClient.builder()
                .region(Region.fromRegionId(region))
                .build(provider);

        // 버킷 정보를 멤버 변수에 저장
        this.namespace = namespace;
        this.bucketName = bucketName;
        this.region = region;
    }

    /**
     * 파일을 OCI Object Storage에 업로드하고 공개 URL을 반환합니다.
     *
     * @param file 업로드할 MultipartFile 객체
     * @return 업로드된 파일의 공개 URL (String)
     */
    public String upload(MultipartFile file) {
        try {
            // 1. 파일이 겹치지 않게 고유한 파일 이름 생성 (UUID 사용)
            // 예: "a1b2c3d4-..." + "_" + "original_filename.jpg"
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // 2. OCI 업로드 요청서(PutObjectRequest) 생성
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(this.namespace)      // OCI 네임스페이스
                    .bucketName(this.bucketName)        // 버킷 이름
                    .objectName(uniqueFileName)         // OCI에 저장될 파일 이름
                    .putObjectBody(file.getInputStream()) // 파일의 실제 데이터 (InputStream)
                    .contentType(file.getContentType()) // 파일의 MIME 타입 (예: "image/jpeg")
                    .build();

            // 3. OCI 클라이언트로 업로드 실행
            client.putObject(request);

            // 4. 업로드된 파일의 공개 URL 조립하여 반환
            // (버킷이 'Public'일 때만 이 URL이 작동합니다)
            // 형식: https://{namespace}.objectstorage.{region}.oraclecloud.com/n/{namespace}/b/{bucketName}/o/{objectName}
            return String.format("https://%s.objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                    this.namespace,
                    this.region,
                    this.namespace,
                    this.bucketName,
                    uniqueFileName
            );

        } catch (IOException e) {
            // 예외 처리: 실제 서비스에서는 로깅을 하거나, 커스텀 예외를 던져야 합니다.
            throw new RuntimeException("OCI 파일 업로드에 실패했습니다.", e);
        }
    }
}