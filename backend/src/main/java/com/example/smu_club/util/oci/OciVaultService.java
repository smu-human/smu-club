package com.example.smu_club.util.oci;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

import java.util.Base64;
import java.util.Map;

public class OciVaultService implements EnvironmentPostProcessor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // [디버깅] 이 로그가 안 보이면 imports 파일 설정이 잘못된 것임
        System.out.println("🔥 [OCI Vault] 초기화 시작! (EnvironmentPostProcessor 진입)");

        // 1. prod 프로필일 때만 작동
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            System.out.println("⚠️ [OCI Vault] 현재 프로필이 'prod'가 아니므로 Vault 로드를 건너뜁니다.");
            return;
        }

        System.out.println("🚀 [OCI Vault] 'prod' 프로필 감지됨. Vault 접속을 시도합니다...");

        // ▼▼▼ [수정됨] 하드코딩한 Secret OCID (오사카 리전) ▼▼▼
        String secretId = "ocid1.vaultsecret.oc1.ap-osaka-1.amaaaaaa5mz35fyafxs5ofmr72yjgrtce3r6hfypgen2afavot46czwuny7a";

        try {
            // 2. Vault에서 시크릿 로드
            Map<String, Object> secrets = fetchSecrets(secretId);

            // [디버깅] 가져온 키 목록 확인 (값은 보안상 일부만 출력하거나 키만 출력)
            System.out.println("✅ [OCI Vault] 시크릿 로드 성공! 가져온 항목 수: " + secrets.size());

            if (secrets.containsKey("JWT_SECRET")) {
                System.out.println("🔑 [확인] 'JWT_SECRET' 키가 존재합니다.");
            } else {
                System.err.println("❌ [주의] Vault에서 데이터를 가져왔으나 'JWT_SECRET' 키가 없습니다! (JSON 키 이름 확인 필요)");
                System.out.println("📜 가져온 전체 키 목록: " + secrets.keySet());
            }

            // 3. 환경변수 등록 (가장 높은 우선순위)
            environment.getPropertySources().addFirst(new MapPropertySource("ociVaultSecrets", secrets));
            System.out.println("✅ [OCI Vault] Spring Environment에 시크릿 주입 완료.");

        } catch (Exception e) {
            // 치명적 에러: 로그 찍고 서버 시작 중단
            System.err.println("☠️ [OCI Vault] 시크릿 로드 중 치명적 오류 발생!");
            e.printStackTrace();
            throw new IllegalStateException("[OCI Vault] 시크릿 로드 실패, 앱 시작을 중단합니다.", e);
        }
    }

    private Map<String, Object> fetchSecrets(String secretId) throws Exception {
        // OCI 인스턴스 인증 (배포 환경에서만 작동)
        InstancePrincipalsAuthenticationDetailsProvider provider =
                InstancePrincipalsAuthenticationDetailsProvider.builder().build();

        try (SecretsClient secretsClient = SecretsClient.builder().build(provider)) {
            GetSecretBundleRequest request = GetSecretBundleRequest.builder()
                    .secretId(secretId)
                    .build();

            GetSecretBundleResponse response = secretsClient.getSecretBundle(request);

            Base64SecretBundleContentDetails content =
                    (Base64SecretBundleContentDetails) response.getSecretBundle().getSecretBundleContent();

            // Java 표준 Base64 디코딩
            String jsonString = new String(Base64.getDecoder().decode(content.getContent()));

            return OBJECT_MAPPER.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        }
    }
}