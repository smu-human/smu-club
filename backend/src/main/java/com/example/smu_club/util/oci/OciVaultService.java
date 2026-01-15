package com.example.smu_club.util.oci;

import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Profile("prod")
public class OciVaultService {

    public Map<String, Object> getSecrets(String secretId) throws Exception {
        // 1. Instance Principal 인증 설정
        // 인스턴스 자체의 메타데이터를 사용하여 인증하므로 설정 파일이 필요 없습니다.
        InstancePrincipalsAuthenticationDetailsProvider provider =
                InstancePrincipalsAuthenticationDetailsProvider.builder().build();

        // 2. Client 생성 (Region은 인스턴스 정보를 통해 자동으로 감지됩니다)
        SecretsClient secretsClient = SecretsClient.builder().build(provider);

        // 3. 비밀 가져오기 요청
        GetSecretBundleRequest getSecretBundleRequest = GetSecretBundleRequest.builder()
                .secretId(secretId)
                .build();

        GetSecretBundleResponse getSecretBundleResponse = secretsClient.getSecretBundle(getSecretBundleRequest);

        // 4. Base64 디코딩
        Base64SecretBundleContentDetails contentDetails =
                (Base64SecretBundleContentDetails) getSecretBundleResponse.getSecretBundle().getSecretBundleContent();

        byte[] decodedBytes = Base64.decodeBase64(contentDetails.getContent());
        String jsonString = new String(decodedBytes);

        // 5. JSON을 Map으로 변환
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, Map.class);
    }
}