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
import org.springframework.boot.logging.DeferredLog; // 스프링 로거 사용
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

import java.util.Base64; // Java 표준 Base64
import java.util.Map;

public class OciVaultService implements EnvironmentPostProcessor {
    private static final DeferredLog log = new DeferredLog();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application){
        //1. prod 프로필일 때만 작동
        if(!environment.acceptsProfiles(Profiles.of("prod"))) {
            log.warn("[Deploy] Profile이 prod가 아닙니다.");
            return;
        }

        String secretId = "ocid1.vaultsecret.oc1.ap-osaka-1.amaaaaaa5mz35fyafxs5ofmr72yjgrtce3r6hfypgen2afavot46czwuny7a";

        try{
            //2. vault에서 시크릿 로드
            Map<String, Object> secrets = fetchSecrets(secretId);

            //3. 환경변수 등록
            environment.getPropertySources().addFirst(new MapPropertySource("ociVaultSecrets", secrets));
        } catch(Exception e){
            throw new IllegalStateException("[OCI Vault] 시크릿 로드 실패, 앱 시작 중단.", e);
        }
    }

    private Map<String, Object> fetchSecrets(String secretId) throws Exception {
        InstancePrincipalsAuthenticationDetailsProvider provider =
                InstancePrincipalsAuthenticationDetailsProvider.builder().build();

        try (SecretsClient secretsClient = SecretsClient.builder().build(provider)) {
            GetSecretBundleRequest request = GetSecretBundleRequest.builder()
                    .secretId(secretId)
                    .build();

            GetSecretBundleResponse response = secretsClient.getSecretBundle(request);

            Base64SecretBundleContentDetails content =
                    (Base64SecretBundleContentDetails) response.getSecretBundle().getSecretBundleContent();

            // Apache Commons 대신 Java 내장 Base64 사용
            String jsonString = new String(Base64.getDecoder().decode(content.getContent()));

            return OBJECT_MAPPER.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
        }
    }
}