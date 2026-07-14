package io.github.eipx.servicefoundation.commons.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Global registry for named password decryption and de-obfuscation callbacks.
 * The built-in {@code clear} provider returns the supplied value unchanged.
 */
public final class PasswordProviderRegistry {

    public static final String CLEAR_PASSWORD_TYPE = "clear";

    private static PasswordProviderRegistry instance;

    private final Map<String, PasswordProvider> passwordProviders = new HashMap<>();

    public static synchronized PasswordProviderRegistry getInstance() {
        if (instance == null) {
            instance = new PasswordProviderRegistry();
        }
        return instance;
    }

    private PasswordProviderRegistry() {
        passwordProviders.put(CLEAR_PASSWORD_TYPE, password -> password);
    }

    public void registerPasswordProvider(String key, PasswordProvider passwordProvider) {
        if (passwordProviders.get(key) != null) {
            throw new IllegalArgumentException("A password provider is already registered for '" + key + "'");
        }
        passwordProviders.put(key, passwordProvider);
    }

    public Optional<PasswordProvider> getPasswordProvider(String key) {
        return Optional.ofNullable(passwordProviders.get(key));
    }
}
