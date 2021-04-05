package ru.sberinsur.insure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberinsur.audit.Audit;
import ru.sberinsur.audit.AuditEventFormatter;
import ru.sberinsur.audit.config.AuditConfig;
import ru.sberinsur.audit.factory.AuditEventFactory;
import ru.sberinsur.audit.factory.KafkaConnectionInfoAdapter;
import ru.sberinsur.audit.logging.LoggingAudit;
import ru.sberinsur.audit.model.ConnectionInfo;

/**
 * Audit configuration
 */
@Configuration
@RefreshScope
public class AuditConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "audit")
    public AuditConfig provideAuditConfig() {
        return new AuditConfig();
    }

    @Bean
    public Audit provideAudit(AuditConfig auditConfig) {
        return LoggingAudit.create(new AuditEventFormatter(), auditConfig.getMarkerName());
    }

    @Bean
    public AuditEventFactory provideAuditEventFactory(AuditConfig auditConfig, KafkaConfig kafkaConfig) {
        ConnectionInfo connectionInfo = KafkaConnectionInfoAdapter.forConnectionString(kafkaConfig.getBootstrapServers());
        return new AuditEventFactory(
                auditConfig,
                new ConnectionInfo(connectionInfo.getProtocol(), connectionInfo.getHostname(), connectionInfo.getPort()),
                null
        );
    }
}
