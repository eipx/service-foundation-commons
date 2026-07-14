package io.github.eipx.servicefoundation.commons.observability.cef;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.lang.Nullable;

import com.google.common.collect.Maps;

import static java.util.Objects.*;

public final class CEFEvent {

    private final int version;
    private final String deviceVendor;
    private final String deviceProduct;
    private final String deviceVersion;
    private final String signatureId;
    private final String name;
    private final Severity severity;
    private final String cs1;
    private final String cs1Label;
    private final String cs2;
    private final String cs2Label;
    private final String cs3;
    private final String cs3Label;
    private final String cs4;
    private final String cs4Label;
    private final String msg;
    private final String cat;
    private final String deviceProcessName;
    private final String src;
    private final String dtz;
    private final Long rt;

    public CEFEvent(int version,
                    String deviceVendor,
                    String deviceProduct,
                    String deviceVersion,
                    String signatureId,
                    String name,
                    Severity severity,
                    @Nullable String cs1,
                    @Nullable String cs1Label,
                    @Nullable String cs2,
                    @Nullable String cs2Label,
                    @Nullable String cs3,
                    @Nullable String cs3Label,
                    @Nullable String cs4,
                    @Nullable String cs4Label,
                    @Nullable String msg,
                    @Nullable String cat,
                    @Nullable String deviceProcessName,
                    @Nullable String src,
                    @Nullable String dtz,
                    @Nullable Long rt) {
        this.version = version;
        this.deviceVendor = requireNonNull(deviceVendor, "deviceVendor");
        this.deviceProduct = requireNonNull(deviceProduct, "deviceProduct");
        this.deviceVersion = requireNonNull(deviceVersion, "deviceVersion");
        this.signatureId = requireNonNull(signatureId, "signatureId");
        this.name = requireNonNull(name, "name");
        this.severity = requireNonNull(severity, "severity");
        this.cs1 = cs1;
        this.cs1Label = cs1Label;
        this.cs2 = cs2;
        this.cs2Label = cs2Label;
        this.cs3 = cs3;
        this.cs3Label = cs3Label;
        this.cs4 = cs4;
        this.cs4Label = cs4Label;
        this.msg = msg;
        this.cat = cat;
        this.deviceProcessName = deviceProcessName;
        this.src = src;
        this.dtz = dtz;
        this.rt = rt;
    }

    public int getVersion() {
        return version;
    }

    public String getDeviceVendor() {
        return deviceVendor;
    }

    public String getDeviceProduct() {
        return deviceProduct;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public String getSignatureId() {
        return signatureId;
    }

    public String getName() {
        return name;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getCs1() {
        return cs1;
    }

    public String getCs1Label() {
        return cs1Label;
    }

    public String getCs2() {
        return cs2;
    }

    public String getCs2Label() {
        return cs2Label;
    }

    public String getCs3() {
        return cs3;
    }

    public String getCs3Label() {
        return cs3Label;
    }

    public String getCs4() {
        return cs4;
    }

    public String getCs4Label() {
        return cs4Label;
    }

    public String getMsg() {
        return msg;
    }

    public String getCat() {
        return cat;
    }

    public String getDeviceProcessName() {
        return deviceProcessName;
    }

    public String getSrc() {
        return src;
    }

    public String getDtz() {
        return dtz;
    }

    public Long getRt() {
        return rt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            CEFEvent cefEvent = (CEFEvent) o;
            return version == cefEvent.version &&
                    Objects.equals(deviceVendor, cefEvent.deviceVendor) &&
                    Objects.equals(deviceProduct, cefEvent.deviceProduct) &&
                    Objects.equals(deviceVersion, cefEvent.deviceVersion) &&
                    Objects.equals(signatureId, cefEvent.signatureId) &&
                    Objects.equals(name, cefEvent.name) &&
                    severity == cefEvent.severity &&
                    Objects.equals(cs1, cefEvent.cs1) &&
                    Objects.equals(cs1Label, cefEvent.cs1Label) &&
                    Objects.equals(cs2, cefEvent.cs2) &&
                    Objects.equals(cs2Label, cefEvent.cs2Label) &&
                    Objects.equals(cs3, cefEvent.cs3) &&
                    Objects.equals(cs3Label, cefEvent.cs3Label) &&
                    Objects.equals(cs4, cefEvent.cs4) &&
                    Objects.equals(cs4Label, cefEvent.cs4Label) &&
                    Objects.equals(msg, cefEvent.msg) &&
                    Objects.equals(cat, cefEvent.cat) &&
                    Objects.equals(deviceProcessName, cefEvent.deviceProcessName) &&
                    Objects.equals(src, cefEvent.src) &&
                    Objects.equals(dtz, cefEvent.dtz) &&
                    Objects.equals(rt, cefEvent.rt);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                version,
                deviceVendor,
                deviceProduct,
                deviceVersion,
                signatureId,
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
                rt
        );
    }

    @Override
    public String toString() {
        return "CEFEvent{" +
                "version=" + version +
                ", deviceVendor='" + deviceVendor + '\'' +
                ", deviceProduct='" + deviceProduct + '\'' +
                ", deviceVersion='" + deviceVersion + '\'' +
                ", signatureId='" + signatureId + '\'' +
                ", name='" + name + '\'' +
                ", severity=" + severity +
                ", cs1='" + cs1 + '\'' +
                ", cs1Label='" + cs1Label + '\'' +
                ", cs2='" + cs2 + '\'' +
                ", cs2Label='" + cs2Label + '\'' +
                ", cs3='" + cs3 + '\'' +
                ", cs3Label='" + cs3Label + '\'' +
                ", cs4='" + cs4 + '\'' +
                ", cs4Label='" + cs4Label + '\'' +
                ", msg='" + msg + '\'' +
                ", cat='" + cat + '\'' +
                ", deviceProcessName='" + deviceProcessName + '\'' +
                ", src='" + src + '\'' +
                ", dtz='" + dtz + '\'' +
                ", rt=" + rt +
                '}';
    }

    public enum Severity {

        Low("Low"), Medium("Medium"), High("High"), VeryHigh("Very-High");

        private static final Map<String, Severity> SEVERITIES_INDEXED_BY_VALUE = Maps.uniqueIndex(Arrays.asList(Severity.values()), Severity::getValue);

        private final String value;

        Severity(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Optional<Severity> fromValue(String value) {
            return Optional.ofNullable(SEVERITIES_INDEXED_BY_VALUE.get(value));
        }
    }

}
