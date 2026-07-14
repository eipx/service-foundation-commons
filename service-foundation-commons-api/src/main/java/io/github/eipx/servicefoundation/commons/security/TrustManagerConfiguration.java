package io.github.eipx.servicefoundation.commons.security;

import java.security.KeyStore;

import javax.net.ssl.TrustManagerFactory;

public class TrustManagerConfiguration extends KeyStoreConfiguration {

    private TrustManagerFactory factory;

    public TrustManagerFactory getTrustManagerFactory() {
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
                clearPasswords();
                factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init(store);
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to create trust manager factory", exception);
        }
    }
}
