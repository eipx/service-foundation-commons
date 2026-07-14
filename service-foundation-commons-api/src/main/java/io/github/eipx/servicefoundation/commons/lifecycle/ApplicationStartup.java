package io.github.eipx.servicefoundation.commons.lifecycle;

/**
 * Business startup callback invoked synchronously during the final startup phase.
 */
@FunctionalInterface
public interface ApplicationStartup {

    void syncStartup();
}
