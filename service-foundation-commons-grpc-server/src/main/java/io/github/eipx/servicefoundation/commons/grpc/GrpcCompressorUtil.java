package io.github.eipx.servicefoundation.commons.grpc;

import java.util.List;

import org.springframework.lang.Nullable;

import io.grpc.Codec;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;

/** Creates the compressor registries supported by the legacy gRPC starter. */
public final class GrpcCompressorUtil {

    public static final String GZIP_CODEC_NAME = new Codec.Gzip().getMessageEncoding();
    public static final String IDENTITY_CODEC_NAME = Codec.Identity.NONE.getMessageEncoding();

    @Nullable
    public static CompressorRegistry createCompressorRegistry(List<String> compressorEncodings) {
        if (compressorEncodings.isEmpty()) {
            return null;
        }
        CompressorRegistry compressorRegistry = CompressorRegistry.newEmptyInstance();
        compressorEncodings.stream()
                .map(GrpcCompressorUtil::resolveCodec)
                .forEach(compressorRegistry::register);
        return compressorRegistry;
    }

    @Nullable
    public static DecompressorRegistry createDecompressorRegistry(List<String> decompressorEncodings) {
        if (decompressorEncodings.isEmpty()) {
            return null;
        }
        DecompressorRegistry decompressorRegistry = DecompressorRegistry.emptyInstance();
        for (String decompressorEncoding : decompressorEncodings) {
            decompressorRegistry = decompressorRegistry.with(resolveCodec(decompressorEncoding), true);
        }
        return decompressorRegistry;
    }

    private static Codec resolveCodec(String name) {
        if (IDENTITY_CODEC_NAME.equals(name)) {
            return Codec.Identity.NONE;
        }
        // Keep the 3.0.4 behavior, including its suffix matching.
        if (GZIP_CODEC_NAME.endsWith(name)) {
            return new Codec.Gzip();
        }
        throw new IllegalArgumentException("Unsupported codec name: " + name);
    }

    private GrpcCompressorUtil() {
    }
}
