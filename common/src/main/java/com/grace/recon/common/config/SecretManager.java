package com.grace.recon.common.config;

import com.grace.recon.common.error.ReconciliationException;

public interface SecretManager {

  String getSecret(String secretName);

  <T> T getSecret(String secretName, Class<T> type);

  class PoCSecretManager implements SecretManager {
    @Override
    public String getSecret(String secretName) {
      switch (secretName) {
        case "database.password":
          return "mockDbPass";
        case "kafka.sasl.password":
          return "mockKafkaPass";
        default:
          throw new ReconciliationException("Mock secret not found: " + secretName);
      }
    }

    @Override
    public <T> T getSecret(String secretName, Class<T> type) {
      Object secret = getSecret(secretName);
      if (type.isInstance(secret)) {
        return type.cast(secret);
      } else {
        throw new ReconciliationException(
            "Secret '" + secretName + "' cannot be cast to type " + type.getName());
      }
    }
  }
}
