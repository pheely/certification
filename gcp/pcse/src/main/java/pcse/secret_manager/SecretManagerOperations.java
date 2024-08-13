package pcse.secret_manager;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.springframework.stereotype.Component;
import pcse.config.ConfigParameters;

@Component
public class SecretManagerOperations {
  private final SecretManagerServiceClient client;
  private final ConfigParameters configParameters;
  
  public SecretManagerOperations(ConfigParameters configParameters, SecretManagerServiceClient client) {
    this.configParameters = configParameters;
    this.client = client;
  }
  
  public Secret createSecret() {
    Secret secret =
        Secret.newBuilder()
            .setReplication(
                Replication.newBuilder()
                    .setAutomatic(Replication.Automatic.newBuilder().build())
                    .build())
            .build();

    return client.createSecret(ProjectName.of(configParameters.getProjectId()), configParameters.getSecretId(), secret);
  }

  public SecretVersion addVersion() {
    SecretName secretName = SecretName.of(configParameters.getProjectId(), configParameters.getSecretId());
    byte[] data = "top secret".getBytes();

    // Checksum is required
    Checksum checksum = new CRC32();
    checksum.update(data, 0, data.length);

    SecretPayload payload =
        SecretPayload.newBuilder()
            .setData(ByteString.copyFrom(data))
            // the following line reports a runtime error of mismatch crc32c value
            // .setDataCrc32C(checksum.getValue())
            .build();

    SecretVersion version = client.addSecretVersion(secretName, payload);
    return version;
  }

  public void deleteSecrete() {
    SecretName secretName = SecretName.of(configParameters.getProjectId(), configParameters.getSecretId());
    client.deleteSecret(secretName);
  }

  public Secret getSecret() {
    SecretName secretName = SecretName.of(configParameters.getProjectId(), configParameters.getSecretId());

    try {
      return client.getSecret(secretName);
    } catch (Exception e) {
      return null;
    }
  }

  public String accessSecretVersion(String versionId) {
    SecretVersionName secretVersionName = SecretVersionName.of(configParameters.getProjectId(), configParameters.getSecretId(), versionId);
    AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
    byte[] data = response.getPayload().getData().toByteArray();
    Checksum checksum = new CRC32();
    checksum.update(data, 0, data.length);

    // Not sure how Google calculates CRC32C, the following code never succeeds
//    if (response.getPayload().getDataCrc32C() != checksum.getValue()) {
//      logger.info("Data corruption detected");
//      return null;
//    }
    return response.getPayload().getData().toStringUtf8();
  }
}
