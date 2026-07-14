package io.github.eipx.servicefoundation.commons.security;

/**
 * Contract for configuration that must build its runtime objects and then erase password data.
 */
public interface SensitiveConfigProperties {

    void initThenClearSensitiveData();
}
