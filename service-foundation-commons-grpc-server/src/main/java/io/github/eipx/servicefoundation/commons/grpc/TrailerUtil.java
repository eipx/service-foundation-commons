package io.github.eipx.servicefoundation.commons.grpc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

import io.grpc.Metadata;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

/** Thread-local status trailer support compatible with the established generic-error trailer contract. */
public final class TrailerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrailerUtil.class);
    private static final String ERROR_DETAILS_METADATA = "error-details" + Metadata.BINARY_HEADER_SUFFIX;

    public static final Metadata.Key<byte[]> ERROR_DETAILS_KEY =
            Metadata.Key.of(ERROR_DETAILS_METADATA, Metadata.BINARY_BYTE_MARSHALLER);

    private static final ThreadLocal<Metadata> THREAD_METADATA = new ThreadLocal<>();

    public static GenericErrorDetails getErrorDetails(Metadata metadata) {
        if (metadata == null) {
            return null;
        }
        byte[] serialized = metadata.get(ERROR_DETAILS_KEY);
        if (serialized == null) {
            return null;
        }
        try {
            CodedInputStream input = CodedInputStream.newInstance(serialized);
            int errorCode = 0;
            String details = "";
            while (!input.isAtEnd()) {
                int tag = input.readTag();
                if (tag == 0) {
                    break;
                }
                int fieldNumber = WireFormat.getTagFieldNumber(tag);
                int wireType = WireFormat.getTagWireType(tag);
                if (fieldNumber == 1 && wireType == WireFormat.WIRETYPE_VARINT) {
                    errorCode = input.readEnum();
                } else if (fieldNumber == 2 && wireType == WireFormat.WIRETYPE_LENGTH_DELIMITED) {
                    details = input.readStringRequireUtf8();
                } else if (!input.skipField(tag)) {
                    break;
                }
            }
            return new GenericErrorDetails(errorCode, details, serialized);
        } catch (IOException | RuntimeException exception) {
            LOGGER.trace("Failed parsing generic error descriptor", exception);
            return null;
        }
    }

    public static GenericErrorDetails getErrorDetails(Throwable throwable) {
        Metadata metadata = null;
        if (throwable instanceof StatusException statusException) {
            metadata = statusException.getTrailers();
        } else if (throwable instanceof StatusRuntimeException statusRuntimeException) {
            metadata = statusRuntimeException.getTrailers();
        }
        return getErrorDetails(metadata);
    }

    public static Metadata translateErrorToMetadata(byte[] errorDetails) {
        Metadata metadata = new Metadata();
        metadata.put(ERROR_DETAILS_KEY, errorDetails);
        return metadata;
    }

    public static Metadata getMetadata() {
        Metadata metadata = THREAD_METADATA.get();
        return metadata == null ? new Metadata() : metadata;
    }

    public static void resetMetadata() {
        THREAD_METADATA.remove();
    }

    public static void setMetadata(byte[] errorDetails) {
        THREAD_METADATA.set(translateErrorToMetadata(errorDetails));
    }

    public static void setMetadata(Metadata metadata) {
        THREAD_METADATA.set(metadata);
    }

    private TrailerUtil() {
    }
}
