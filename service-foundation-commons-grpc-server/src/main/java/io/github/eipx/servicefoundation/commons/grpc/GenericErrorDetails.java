package io.github.eipx.servicefoundation.commons.grpc;

import java.util.Arrays;

import com.google.common.base.MoreObjects;

/** Parsed representation of the legacy generic-error trailer payload. */
public class GenericErrorDetails {

    private final ErrorCode errorCode;
    private final int originalErrorCode;
    private final String errorDetails;
    private final byte[] originalError;

    public GenericErrorDetails(int errorCodeIndex, String errorDetails, byte[] originalError) {
        this.errorCode = ErrorCode.fromIndex(errorCodeIndex);
        this.originalErrorCode = errorCodeIndex;
        this.errorDetails = errorDetails;
        this.originalError = originalError;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getOriginalErrorCode() {
        return originalErrorCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public byte[] getOriginalError() {
        return Arrays.copyOf(originalError, originalError.length);
    }

    public enum ErrorCode {
        UNKNOWN_ERROR,
        SHUTTING_DOWN,
        NO_SERVICE;

        static ErrorCode fromIndex(int index) {
            return switch (index) {
                case 1 -> SHUTTING_DOWN;
                case 2 -> NO_SERVICE;
                default -> UNKNOWN_ERROR;
            };
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("errorCode", errorCode)
                .add("originalErrorCode", originalErrorCode)
                .add("errorDetails", errorDetails)
                .add("originalError", originalError)
                .toString();
    }
}
