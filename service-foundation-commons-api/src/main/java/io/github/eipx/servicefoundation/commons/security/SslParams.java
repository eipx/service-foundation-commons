package io.github.eipx.servicefoundation.commons.security;

import java.util.List;

public class SslParams implements SensitiveConfigProperties {

    private boolean enabled;
    private Integer sessionCacheSize;
    private Integer sessionTimeoutInSeconds;
    private ClientAuthentication clientAuth = ClientAuthentication.NONE;
    private List<String> enabledCipherSuites;
    private List<String> enabledProtocols;
    private final TrustManagerConfiguration trustManager = new TrustManagerConfiguration();
    private final KeyManagerConfiguration keyManager = new KeyManagerConfiguration();
    private boolean gsrCompliant = true;

    @Override
    public void initThenClearSensitiveData() {
        if (enabled) {
            trustManager.initThenClearSensitiveData();
            keyManager.initThenClearSensitiveData();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSessionCacheSize() {
        return sessionCacheSize;
    }

    public void setSessionCacheSize(Integer sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
    }

    public Integer getSessionTimeoutInSeconds() {
        return sessionTimeoutInSeconds;
    }

    public void setSessionTimeoutInSeconds(Integer sessionTimeoutInSeconds) {
        this.sessionTimeoutInSeconds = sessionTimeoutInSeconds;
    }

    public ClientAuthentication getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(ClientAuthentication clientAuth) {
        this.clientAuth = clientAuth;
    }

    public List<String> getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    public void setEnabledCipherSuites(List<String> enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }

    public List<String> getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(List<String> enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    public boolean isGsrCompliant() {
        return gsrCompliant;
    }

    public void setGsrCompliant(boolean gsrCompliant) {
        this.gsrCompliant = gsrCompliant;
    }

    public TrustManagerConfiguration getTrustManager() {
        return trustManager;
    }

    public KeyManagerConfiguration getKeyManager() {
        return keyManager;
    }
}
