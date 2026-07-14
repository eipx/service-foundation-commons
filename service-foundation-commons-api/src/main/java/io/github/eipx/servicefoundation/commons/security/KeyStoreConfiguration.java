package io.github.eipx.servicefoundation.commons.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;

/**
 * Maps key-store configuration properties to a lazily created {@link KeyStore}.
 *
 * <p>Legacy pseudo-URI handling is intentionally absent. This implementation uses file-backed JKS stores.
 */
public abstract class KeyStoreConfiguration implements SensitiveConfigProperties {

    public static final String DEFAULT_KEY_STORE_TYPE = "PKCS12";
    public static final String DEFAULT_KEY_STORE_PASSWORD_TYPE = PasswordProviderRegistry.CLEAR_PASSWORD_TYPE;

    private String keyStoreUri;
    private byte[] keyStoreBytes;
    private String keyStoreType = DEFAULT_KEY_STORE_TYPE;
    private PasswordProvider passwordProvider = PasswordProviderRegistry.getInstance()
            .getPasswordProvider(DEFAULT_KEY_STORE_PASSWORD_TYPE)
            .orElseThrow(() -> new IllegalStateException("Expected clear password type to be registered."));
    private KeyStore keyStore;
    private char[] keyStorePassword = new char[0];
    private char[] decryptedKeyStorePassword;

    public static KeyStore buildKeyStore(String keyStoreUri, char[] keyStorePassword) {
        return buildKeyStore(keyStoreUri, DEFAULT_KEY_STORE_TYPE, keyStorePassword);
    }

    public static KeyStore buildKeyStore(String keyStoreUri, String keyStoreType, char[] keyStorePassword) {
        try {
            KeyStore store = KeyStore.getInstance(keyStoreType);
            try (InputStream input = newInputStreamFromUri(keyStoreUri)) {
                store.load(input, keyStorePassword);
            }
            return store;
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Failed to read key store of type '" + keyStoreType + "' at '" + keyStoreUri + "'",
                    exception);
        }
    }

    public static KeyStore buildKeyStore(byte[] keyStoreBytes, char[] keyStorePassword) {
        return buildKeyStore(keyStoreBytes, DEFAULT_KEY_STORE_TYPE, keyStorePassword);
    }

    public static KeyStore buildKeyStore(byte[] keyStoreBytes, String keyStoreType, char[] keyStorePassword) {
        try {
            KeyStore store = KeyStore.getInstance(keyStoreType);
            try (InputStream input = new ByteArrayInputStream(keyStoreBytes)) {
                store.load(input, keyStorePassword);
            }
            return store;
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Failed to read key store of type '" + keyStoreType + "' from bytes",
                    exception);
        }
    }

    private static InputStream newInputStreamFromUri(String value) throws IOException {
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException exception) {
            uri = null;
        }

        if (uri == null || !uri.isAbsolute()) {
            return Files.newInputStream(Paths.get(value));
        }
        if ("file".equals(uri.getScheme())) {
            return Files.newInputStream(Path.of(uri));
        }
        if ("classpath".equals(uri.getScheme())) {
            InputStream input = KeyStoreConfiguration.class.getResourceAsStream(uri.getPath());
            if (input == null) {
                throw new IOException("Classpath resource does not exist: " + uri.getPath());
            }
            return input;
        }
        throw new UnsupportedOperationException("Unsupported URI scheme: " + uri.getScheme());
    }

    public String getKeyStoreUri() {
        return keyStoreUri;
    }

    public void setKeyStoreUri(String keyStoreUri) {
        this.keyStoreUri = keyStoreUri;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public char[] getKeyStorePassword() {
        if (decryptedKeyStorePassword == null) {
            decryptedKeyStorePassword = passwordProvider.getPassword(keyStorePassword);
        }
        return decryptedKeyStorePassword;
    }

    public void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setKeyStorePasswordType(String keyStorePasswordType) {
        passwordProvider = PasswordProviderRegistry.getInstance()
                .getPasswordProvider(keyStorePasswordType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registry does not have provider for password type '" + keyStorePasswordType + "'"));
    }

    public void setKeyStoreBytes(byte[] keyStoreBytes) {
        this.keyStoreBytes = keyStoreBytes;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public KeyStore getKeyStore() {
        if (keyStore != null) {
            return keyStore;
        }
        if (keyStoreUri != null) {
            keyStore = buildKeyStore(keyStoreUri, keyStoreType, getKeyStorePassword());
        }
        if (keyStoreBytes != null) {
            keyStore = buildKeyStore(keyStoreBytes, keyStoreType, getKeyStorePassword());
        }
        return keyStore;
    }

    public void clearPasswords() {
        if (keyStorePassword != null) {
            Arrays.fill(keyStorePassword, (char) 0);
        }
        if (decryptedKeyStorePassword != null) {
            Arrays.fill(decryptedKeyStorePassword, (char) 0);
        }
    }
}
