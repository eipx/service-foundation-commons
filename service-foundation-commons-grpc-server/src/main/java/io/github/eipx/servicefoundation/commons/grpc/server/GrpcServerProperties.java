package io.github.eipx.servicefoundation.commons.grpc.server;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.eipx.servicefoundation.commons.grpc.GrpcNettySslParams;
import io.github.eipx.servicefoundation.commons.security.SensitiveConfigProperties;

import jakarta.annotation.PostConstruct;

@SuppressWarnings({"WeakerAccess", "unused"})
@ConfigurationProperties("grpc.server")
public class GrpcServerProperties implements SensitiveConfigProperties {

    private int port = 9090;
    private String host = "0.0.0.0";
    private Boolean permitKeepAliveWithoutCalls;
    private Long keepAliveTimeInSeconds;
    private Long keepAliveTimeoutInSeconds;
    private Long permitKeepAliveTimeInSeconds;
    private Long maxConnectionAgeInSeconds;
    private Long maxConnectionIdleInSeconds;
    private Integer flowControlWindow;
    private Integer maxMessageSize;
    private Integer maxHeaderListSize;
    private Integer maxConnectionAgeGraceInSeconds;
    private Integer maxConcurrentCallsPerConnection;
    private List<String> compressorEncodings;
    private List<String> decompressorEncodings;
    private final GrpcNettySslParams security = new GrpcNettySslParams();

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    public void setPermitKeepAliveWithoutCalls(Boolean permitKeepAliveWithoutCalls) {
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
    }

    public Long getKeepAliveTimeInSeconds() {
        return keepAliveTimeInSeconds;
    }

    public void setKeepAliveTimeInSeconds(Long keepAliveTimeInSeconds) {
        this.keepAliveTimeInSeconds = keepAliveTimeInSeconds;
    }

    public Long getKeepAliveTimeoutInSeconds() {
        return keepAliveTimeoutInSeconds;
    }

    public void setKeepAliveTimeoutInSeconds(Long keepAliveTimeoutInSeconds) {
        this.keepAliveTimeoutInSeconds = keepAliveTimeoutInSeconds;
    }

    public Long getPermitKeepAliveTimeInSeconds() {
        return permitKeepAliveTimeInSeconds;
    }

    public void setPermitKeepAliveTimeInSeconds(Long permitKeepAliveTimeInSeconds) {
        this.permitKeepAliveTimeInSeconds = permitKeepAliveTimeInSeconds;
    }

    public Long getMaxConnectionAgeInSeconds() {
        return maxConnectionAgeInSeconds;
    }

    public void setMaxConnectionAgeInSeconds(Long maxConnectionAgeInSeconds) {
        this.maxConnectionAgeInSeconds = maxConnectionAgeInSeconds;
    }

    public Long getMaxConnectionIdleInSeconds() {
        return maxConnectionIdleInSeconds;
    }

    public void setMaxConnectionIdleInSeconds(Long maxConnectionIdleInSeconds) {
        this.maxConnectionIdleInSeconds = maxConnectionIdleInSeconds;
    }

    public Integer getFlowControlWindow() {
        return flowControlWindow;
    }

    public void setFlowControlWindow(Integer flowControlWindow) {
        this.flowControlWindow = flowControlWindow;
    }

    public Integer getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(Integer maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public Integer getMaxHeaderListSize() {
        return maxHeaderListSize;
    }

    public void setMaxHeaderListSize(Integer maxHeaderListSize) {
        this.maxHeaderListSize = maxHeaderListSize;
    }

    public Integer getMaxConnectionAgeGraceInSeconds() {
        return maxConnectionAgeGraceInSeconds;
    }

    public void setMaxConnectionAgeGraceInSeconds(Integer maxConnectionAgeGraceInSeconds) {
        this.maxConnectionAgeGraceInSeconds = maxConnectionAgeGraceInSeconds;
    }

    public Integer getMaxConcurrentCallsPerConnection() {
        return maxConcurrentCallsPerConnection;
    }

    public void setMaxConcurrentCallsPerConnection(Integer maxConcurrentCallsPerConnection) {
        this.maxConcurrentCallsPerConnection = maxConcurrentCallsPerConnection;
    }

    public List<String> getCompressorEncodings() {
        return compressorEncodings;
    }

    public void setCompressorEncodings(List<String> compressorEncodings) {
        this.compressorEncodings = compressorEncodings;
    }

    public List<String> getDecompressorEncodings() {
        return decompressorEncodings;
    }

    public void setDecompressorEncodings(List<String> decompressorEncodings) {
        this.decompressorEncodings = decompressorEncodings;
    }

    public GrpcNettySslParams getSecurity() {
        return security;
    }

    @PostConstruct
    @Override
    public void initThenClearSensitiveData() {
        security.initThenClearSensitiveData();
    }
}
