package pcse.config;

import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Clients {
  @Bean
  public KeyManagementServiceClient getKMSClient() throws IOException {
    KeyManagementServiceClient client = KeyManagementServiceClient.create();
    return client;
  }

  @Bean
  public SecretManagerServiceClient getSecretManagerClient() throws IOException {
    SecretManagerServiceClient client = SecretManagerServiceClient.create();
    return client;
  }
}
