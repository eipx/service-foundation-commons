package io.github.eipx.servicefoundation.commons.security;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

public class KeyManagerConfiguration extends KeyStoreConfiguration {

    private KeyManagerFactory factory;

    public KeyManagerFactory getKeyManagerFactory() {
        if (factory == null) {
            throw new IllegalStateException("Not initialized");
        }
        return factory;
    }

    @Override
    public void initThenClearSensitiveData() {
        try {
            KeyStore store = getKeyStore();
            if (store != null) {
                factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                factory.init(store, getKeyStorePassword());
            }
            clearPasswords();
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Failed to create key manager factory: " + exception.getMessage(), exception);
        }
    }
}
