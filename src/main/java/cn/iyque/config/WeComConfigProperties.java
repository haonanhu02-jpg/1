package cn.iyque.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wecom")
@Data
public class WeComConfigProperties {

    private String corpId;

    private String agentId;

    private String agentSecret;

    private String token;

    private String encodingAesKey;

    private String msgAuditLibPath;

    private String msgAuditPrivateKey;

    private String msgAuditSecret;
}
