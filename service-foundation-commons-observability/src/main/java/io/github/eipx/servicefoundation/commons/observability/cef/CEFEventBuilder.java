package io.github.eipx.servicefoundation.commons.observability.cef;

/**
 * A builder to build {@link CEFEvent}s that can be logged in the event journal
 */
public class CEFEventBuilder {

    private String name;
    private CEFEvent.Severity severity;
    private String msg;
    private String cat;
    private String component;
    private int code;
    private int version;
    private String deviceVendor = "";
    private String deviceProduct = "";
    private String deviceVersion = "";
    private String cs1;
    private String cs1Label;
    private String cs2;
    private String cs2Label;
    private String cs3;
    private String cs3Label;
    private String cs4;
    private String cs4Label;
    private String deviceProcessName;
    private String src;
    private String dtz;
    private Long rt;

    public CEFEventBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CEFEventBuilder withSeverity(CEFEvent.Severity severity) {
        this.severity = severity;
        return this;
    }

    public CEFEventBuilder withCategory(String category) {
        this.cat = category;
        return this;
    }

    public CEFEventBuilder withComponent(String component) {
        this.component = component;
        return this;
    }

    public CEFEventBuilder withCode(int code) {
        this.code = code;
        return this;
    }

    public CEFEventBuilder withMessage(String message) {
        this.msg = message;
        return this;
    }

    public CEFEventBuilder withVersion(int version) {
        this.version = version;
        return this;
    }

    public CEFEventBuilder withDeviceVendor(String deviceVendor) {
        this.deviceVendor = deviceVendor;
        return this;
    }

    public CEFEventBuilder withDeviceProduct(String deviceProduct) {
        this.deviceProduct = deviceProduct;
        return this;
    }

    public CEFEventBuilder withDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
        return this;
    }

    public CEFEventBuilder withSource(String sourceIP) {
        return withSrc(sourceIP);
    }

    public CEFEventBuilder withSrc(String src) {
        this.src = src;
        return this;
    }

    public CEFEventBuilder withRt(Long rt) {
        this.rt = rt;
        return this;
    }

    public CEFEventBuilder withCs1(String cs1) {
        this.cs1 = cs1;
        return this;
    }

    public CEFEventBuilder withCs1label(String cs1label) {
        this.cs1Label = cs1label;
        return this;
    }

    public CEFEventBuilder withCs2(String cs2) {
        this.cs2 = cs2;
        return this;
    }

    public CEFEventBuilder withCs2label(String label) {
        this.cs2Label = label;
        return this;
    }

    public CEFEventBuilder withCs3(String cs3) {
        this.cs3 = cs3;
        return this;
    }

    public CEFEventBuilder withCs3label(String label) {
        this.cs3Label = label;
        return this;
    }

    public CEFEventBuilder withCs4(String cs4) {
        this.cs4 = cs4;
        return this;
    }

    public CEFEventBuilder withCs4label(String label) {
        this.cs4Label = label;
        return this;
    }

    public CEFEventBuilder withDeviceProcessName(String deviceProcessName) {
        this.deviceProcessName = deviceProcessName;
        return this;
    }

    public CEFEventBuilder withDtz(String dtz) {
        this.dtz = dtz;
        return this;
    }

    public CEFEvent build() {
        return new CEFEvent(version,
                            deviceVendor,
                            deviceProduct,
                            deviceVersion,
                            String.format("%s.%s.%04d", this.deviceProduct, component, code),
                            name,
                            severity,
                            cs1,
                            cs1Label,
                            cs2,
                            cs2Label,
                            cs3,
                            cs3Label,
                            cs4,
                            cs4Label,
                            msg,
                            cat,
                            deviceProcessName,
                            src,
                            dtz,
                            rt);
    }
}
