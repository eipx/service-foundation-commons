package io.github.eipx.servicefoundation.commons.observability.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tracking")
public class TrackingProperties {

    /**
     * Export is opt-in. A missing value keeps tracing context active while spans
     * are discarded locally, matching the legacy runtime behavior.
     */
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
