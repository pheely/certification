package pcse;

import com.google.cloud.kms.v1.KeyRing;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretVersion;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pcse.cloud_kms.SymmetricEncryptionKeyOperations;
import pcse.config.BNSProxySelector;
import pcse.secret_manager.SecretManagerOperations;

@SpringBootApplication
public class Application implements CommandLineRunner {

  private final Logger logger = LoggerFactory.getLogger(Application.class);
  private final SecretManagerOperations sm;
  private final SymmetricEncryptionKeyOperations symmetriOps;

  public Application(SecretManagerOperations sm, SymmetricEncryptionKeyOperations symmetricOps) {
    this.sm = sm;
    this.symmetriOps = symmetricOps;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(String... args) throws Exception {
    BNSProxySelector.applyBNSProxySettings();

    testSecretManager();
  }

  private void testSecretManager() throws IOException {
    if (sm.getSecret() != null) {
      sm.deleteSecrete();
      logger.info("Existing secret deleted: {}");
    }
    Secret secret = sm.createSecret();
      logger.info("Created secret: {}", secret.getName());

    SecretVersion version = sm.addVersion();
    logger.info("Added version: {}", version.getName());

    String versionName = version.getName();
    String secretPayload = sm.accessSecretVersion(versionName.substring(versionName.lastIndexOf('/') + 1));
    logger.info("Secret payload: {}", secretPayload);

    sm.deleteSecrete();
  }

  private void testSymmetricEncryptionOps() throws IOException {
    KeyRing keyRing = symmetriOps.createKeyRing();

  }
}
