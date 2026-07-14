package io.github.eipx.servicefoundation.commons.lifecycle;

/**
 * Business shutdown callback invoked synchronously during the first shutdown phase.
 */
@FunctionalInterface
public interface ApplicationShutdown {

    void syncShutdown();
}
