package io.github.eipx.servicefoundation.commons.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Framework properties retaining the existing unprefixed property contract.
 */
@ConfigurationProperties
public class CoreProperties {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${application.short-name:UNKNWN}")
    private String applicationShortName;

    private String serviceInstanceId;

    @Value("${app-instance-uuid:UNDEFINED}")
    private String appInstanceUUID;

    @Value("${log-dir:logs}")
    private String logDir;

    @Value("${company-name:SAMPLE}")
    private String companyName;

    @Value("${product-name:Unknown}")
    private String productName;

    @Value("${product-version:Unknown}")
    private String productVersion;

    @Value("${app-instance-id:UNDEFINED}")
    private String appInstanceId;

    private int integrityCheckIntervalLowerBound = 60;
    private int integrityCheckIntervalUpperBound = 3600;

    public String getSpringApplicationName() {
        return applicationName;
    }

    public String getApplicationShortName() {
        return applicationShortName;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getAppInstanceUUID() {
        return appInstanceUUID;
    }

    public void setAppInstanceUUID(String appInstanceUUID) {
        this.appInstanceUUID = appInstanceUUID;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getAppInstanceId() {
        return appInstanceId;
    }

    public void setAppInstanceId(String appInstanceId) {
        this.appInstanceId = appInstanceId;
    }

    public int getIntegrityCheckIntervalLowerBound() {
        return integrityCheckIntervalLowerBound;
    }

    public void setIntegrityCheckIntervalLowerBound(int integrityCheckIntervalLowerBound) {
        this.integrityCheckIntervalLowerBound = integrityCheckIntervalLowerBound;
    }

    public int getIntegrityCheckIntervalUpperBound() {
        return integrityCheckIntervalUpperBound;
    }

    public void setIntegrityCheckIntervalUpperBound(int integrityCheckIntervalUpperBound) {
        this.integrityCheckIntervalUpperBound = integrityCheckIntervalUpperBound;
    }
}
