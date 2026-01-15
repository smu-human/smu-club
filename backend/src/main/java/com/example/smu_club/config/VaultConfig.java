package com.example.smu_club.config;

import com.example.smu_club.util.oci.OciVaultService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

@Configuration
@Profile("prod") // 'prod' 프로필일 때만 이 설정 클래스가 동작합니다.
public class VaultConfig implements ApplicationRunner {

    private final OciVaultService ociVaultService;
    private final ConfigurableEnvironment environment;

    public VaultConfig(OciVaultService ociVaultService, ConfigurableEnvironment environment) {
        this.ociVaultService = ociVaultService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String secretId = System.getenv("OCI_SECRET_ID");

        if (secretId != null) {
            Map<String, Object> secrets = ociVaultService.getSecrets(secretId);
            environment.getPropertySources().addFirst(new MapPropertySource("ociVaultSecrets", secrets));
            System.out.println("✅ OCI Vault로부터 시크릿 로드 완료!");
        } else {
            // secretId가 없으면 경고만 띄우고 종료 (로컬 에러 방지용 안전장치)
            System.out.println("⚠️ OCI_SECRET_ID 환경 변수가 없어 Vault를 건너뜁니다.\n로컬 개발 환경에서는 이 메시지가 정상입니다.");
        }
    }
}