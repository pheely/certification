package pcse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties
@Configuration
@Data
public class ConfigParameters {
  private String projectId;
  private String secretId;
  private String location;
  private String keyringId;
  private String keyId;
}
