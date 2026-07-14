package io.github.eipx.servicefoundation.commons.security;

/**
 * Shared socket configuration retained for metrics, tracing, and transport integrations.
 */
public class SocketConfiguration implements SensitiveConfigProperties {

    private Boolean keepAlive;
    private Boolean oobInline;
    private Integer timeoutMillis;
    private String performancePreferences;
    private Integer sendBufferSize;
    private Integer receiveBufferSize;
    private Boolean reuseAddress;
    private Boolean tcpNoDelay;
    private Integer lingerSeconds;
    private final SslParams security = new SslParams();

    public Boolean getKeepAlive() { return keepAlive; }
    public void setKeepAlive(Boolean keepAlive) { this.keepAlive = keepAlive; }
    public Boolean getOobInline() { return oobInline; }
    public void setOobInline(Boolean oobInline) { this.oobInline = oobInline; }
    public Integer getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(Integer timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    public String getPerformancePreferences() { return performancePreferences; }
    public void setPerformancePreferences(String performancePreferences) { this.performancePreferences = performancePreferences; }
    public Integer getSendBufferSize() { return sendBufferSize; }
    public void setSendBufferSize(Integer sendBufferSize) { this.sendBufferSize = sendBufferSize; }
    public Integer getReceiveBufferSize() { return receiveBufferSize; }
    public void setReceiveBufferSize(Integer receiveBufferSize) { this.receiveBufferSize = receiveBufferSize; }
    public Boolean getReuseAddress() { return reuseAddress; }
    public void setReuseAddress(Boolean reuseAddress) { this.reuseAddress = reuseAddress; }
    public Boolean getTcpNoDelay() { return tcpNoDelay; }
    public void setTcpNoDelay(Boolean tcpNoDelay) { this.tcpNoDelay = tcpNoDelay; }
    public Integer getLingerSeconds() { return lingerSeconds; }
    public void setLingerSeconds(Integer lingerSeconds) { this.lingerSeconds = lingerSeconds; }
    public SslParams getSecurity() { return security; }

    @Override
    public void initThenClearSensitiveData() {
        security.initThenClearSensitiveData();
    }
}
