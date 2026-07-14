package io.github.eipx.servicefoundation.commons.compatibility;

import io.github.eipx.servicefoundation.commons.grpc.server.GrpcServerProperties;
import io.github.eipx.servicefoundation.commons.security.ClientAuthentication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyGrpcPropertiesCompatibilityTest {

    @Test
    void bindsTheExistingUrcGrpcServerPropertyNamesWithoutTranslation() {
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(new MapPropertySource("sample", Map.of(
                "grpc.server.port", "8443",
                "grpc.server.security.enabled", "true",
                "grpc.server.security.client-auth", "REQUIRE",
                "grpc.server.security.key-manager.key-store-type", "JKS",
                "grpc.server.security.key-manager.key-store-uri", "file:/etc/service-foundation/service.jks",
                "grpc.server.security.trust-manager.key-store-type", "JKS",
                "grpc.server.security.trust-manager.key-store-uri", "file:/etc/service-foundation/trust.jks")));

        GrpcServerProperties properties = new Binder(ConfigurationPropertySources.from(sources))
                .bind("grpc.server", Bindable.of(GrpcServerProperties.class))
                .get();

        assertEquals(8443, properties.getPort());
        assertTrue(properties.getSecurity().isEnabled());
        assertEquals(ClientAuthentication.REQUIRE, properties.getSecurity().getClientAuth());
        assertEquals("JKS", properties.getSecurity().getKeyManager().getKeyStoreType());
        assertEquals("file:/etc/service-foundation/service.jks", properties.getSecurity().getKeyManager().getKeyStoreUri());
        assertEquals("JKS", properties.getSecurity().getTrustManager().getKeyStoreType());
        assertEquals("file:/etc/service-foundation/trust.jks", properties.getSecurity().getTrustManager().getKeyStoreUri());
    }
}
