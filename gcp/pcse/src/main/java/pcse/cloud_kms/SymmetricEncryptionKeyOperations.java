package pcse.cloud_kms;

import com.google.cloud.kms.v1.CryptoKey;
import com.google.cloud.kms.v1.CryptoKey.CryptoKeyPurpose;
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm;
import com.google.cloud.kms.v1.CryptoKeyVersionTemplate;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyRing;
import com.google.cloud.kms.v1.KeyRingName;
import com.google.cloud.kms.v1.LocationName;
import org.springframework.stereotype.Component;
import pcse.config.ConfigParameters;

@Component
public class SymmetricEncryptionKeyOperations {
  private final ConfigParameters configParameters;
  private final KeyManagementServiceClient keyManagementServiceClient;

  public SymmetricEncryptionKeyOperations(ConfigParameters configParameters,
      KeyManagementServiceClient keyManagementServiceClient) {
    this.configParameters = configParameters;
    this.keyManagementServiceClient = keyManagementServiceClient;
  }

  public KeyRing createKeyRing() {
    KeyRing createdKeyRing = keyManagementServiceClient.createKeyRing(
        LocationName.of(configParameters.getProjectId(), configParameters.getLocation()),
        configParameters.getKeyringId(),
        KeyRing.newBuilder().build()
    );

    return createdKeyRing;
  }

  public CryptoKey createCryptoKey() {
    CryptoKey key = CryptoKey.newBuilder()
        .setPurpose(CryptoKeyPurpose.ENCRYPT_DECRYPT)
        .setVersionTemplate(
            CryptoKeyVersionTemplate.newBuilder()
                .setAlgorithm(CryptoKeyVersionAlgorithm.GOOGLE_SYMMETRIC_ENCRYPTION).build())
        .build();

    CryptoKey createdKey = keyManagementServiceClient.createCryptoKey(
        KeyRingName.of(configParameters.getProjectId(), configParameters.getLocation(), configParameters.getKeyId()),
        configParameters.getKeyId(),
        key);

    return createdKey;6426
  }
}
