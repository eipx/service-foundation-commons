package io.github.eipx.servicefoundation.commons.security;

@FunctionalInterface
public interface PasswordProvider {

    char[] getPassword(char[] metaData);
}
