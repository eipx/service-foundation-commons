package io.github.eipx.servicefoundation.commons.grpc;

import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import io.github.eipx.servicefoundation.commons.security.ClientAuthentication;
import io.github.eipx.servicefoundation.commons.security.SslParams;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

/** Netty/OpenSSL projection of the shared TLS configuration model. */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GrpcNettySslParams extends SslParams {

    private boolean ocspEnabled;

    public boolean isOcspEnabled() {
        return ocspEnabled;
    }

    public void setOcspEnabled(boolean ocspEnabled) {
        this.ocspEnabled = ocspEnabled;
    }

    public SslContext createSslContext(boolean forServer) {
        try {
            KeyManagerFactory keyManagerFactory = forServer || getClientAuth() != ClientAuthentication.NONE
                    ? getKeyManager().getKeyManagerFactory()
                    : null;
            TrustManagerFactory trustManagerFactory = !forServer || getClientAuth() != ClientAuthentication.NONE
                    ? getTrustManager().getTrustManagerFactory()
                    : null;

            SslContextBuilder builder = forServer
                    ? SslContextBuilder.forServer(keyManagerFactory)
                    : SslContextBuilder.forClient();
            builder = customizeBuilder(builder);
            builder.enableOcsp(isOcspEnabled());
            if (keyManagerFactory != null) {
                builder.keyManager(keyManagerFactory);
            }
            if (trustManagerFactory != null) {
                builder.trustManager(trustManagerFactory);
            }
            List<String> enabledCipherSuites = getEnabledCipherSuites();
            if (enabledCipherSuites != null) {
                builder.ciphers(enabledCipherSuites);
            }
            if (getSessionCacheSize() != null) {
                builder.sessionCacheSize(getSessionCacheSize().longValue());
            }
            if (getSessionTimeoutInSeconds() != null) {
                builder.sessionTimeout(getSessionTimeoutInSeconds().longValue());
            }
            List<String> enabledProtocols = getEnabledProtocols();
            if (enabledProtocols != null) {
                builder.protocols(enabledProtocols.toArray(String[]::new));
            }
            return builder.sslProvider(SslProvider.OPENSSL)
                    .clientAuth(toClientAuth(getClientAuth()))
                    .build();
        } catch (SSLException exception) {
            throw new IllegalArgumentException("Failed to create SSL context", exception);
        }
    }

    protected SslContextBuilder customizeBuilder(SslContextBuilder builder) {
        return GrpcSslContexts.configure(builder);
    }

    private static ClientAuth toClientAuth(ClientAuthentication clientAuthentication) {
        return switch (clientAuthentication) {
            case REQUIRE -> ClientAuth.REQUIRE;
            case OPTIONAL -> ClientAuth.OPTIONAL;
            case NONE -> ClientAuth.NONE;
        };
    }
}
