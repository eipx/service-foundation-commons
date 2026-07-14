package io.github.eipx.servicefoundation.commons.compatibility;

import io.github.eipx.servicefoundation.commons.security.PasswordProvider;
import io.github.eipx.servicefoundation.commons.security.PasswordProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordProviderRegistryCompatibilityTest {

    @Test
    void clearProviderReturnsTheOriginalPasswordArray() {
        char[] clearText = "secret".toCharArray();

        PasswordProvider provider = PasswordProviderRegistry.getInstance()
                .getPasswordProvider(PasswordProviderRegistry.CLEAR_PASSWORD_TYPE)
                .orElseThrow();

        assertSame(clearText, provider.getPassword(clearText));
    }

    @Test
    void duplicateProviderRegistrationKeepsTheLegacyFailureContract() {
        String providerName = "compatibility-" + UUID.randomUUID();
        PasswordProviderRegistry registry = PasswordProviderRegistry.getInstance();
        registry.registerPasswordProvider(providerName, metadata -> "decrypted".toCharArray());

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerPasswordProvider(providerName, metadata -> metadata));

        assertEquals("A password provider is already registered for '" + providerName + "'", failure.getMessage());
    }
}
