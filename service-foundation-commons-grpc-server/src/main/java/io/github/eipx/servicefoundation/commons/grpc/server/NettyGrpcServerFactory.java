package io.github.eipx.servicefoundation.commons.grpc.server;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.github.eipx.servicefoundation.commons.grpc.GrpcCompressorUtil;

import brave.grpc.GrpcTracing;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.netty.NettyServerBuilder;

import static java.util.concurrent.TimeUnit.SECONDS;

import static io.grpc.ServerInterceptors.intercept;

public class NettyGrpcServerFactory implements GrpcServerFactory {

    private static final Splitter ACCEPT_ENCODING_SPLITTER = Splitter.on(',').trimResults();
    private static final Metadata.Key<String> MESSAGE_ACCEPT_ENCODING_KEY =
            Metadata.Key.of("grpc-accept-encoding", Metadata.ASCII_STRING_MARSHALLER);
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyGrpcServerFactory.class);

    private final GrpcServerProperties properties;
    private final Collection<GrpcServerServiceDefinition> services;
    private final GrpcTracing grpcTracing;

    public NettyGrpcServerFactory(
            GrpcServerProperties properties,
            Collection<GrpcServerServiceDefinition> services,
            @Nullable GrpcTracing grpcTracing) {
        this.properties = properties;
        this.services = services;
        this.grpcTracing = grpcTracing;
    }

    @Override
    public String getAddress() {
        return properties.getHost();
    }

    @Override
    public int getPort() {
        return properties.getPort();
    }

    @Nullable
    @Override
    public Server createServer() {
        if (services.isEmpty()) {
            return null;
        }

        NettyServerBuilder builder = NettyServerBuilder.forAddress(
                new InetSocketAddress(properties.getHost(), properties.getPort()));
        if (properties.getKeepAliveTimeInSeconds() != null) {
            builder.keepAliveTime(properties.getKeepAliveTimeInSeconds(), SECONDS);
        }
        if (properties.getKeepAliveTimeoutInSeconds() != null) {
            builder.keepAliveTimeout(properties.getKeepAliveTimeoutInSeconds(), SECONDS);
        }
        if (properties.getPermitKeepAliveTimeInSeconds() != null) {
            builder.permitKeepAliveTime(properties.getPermitKeepAliveTimeInSeconds(), SECONDS);
        }
        if (properties.getMaxConnectionAgeGraceInSeconds() != null) {
            builder.maxConnectionAgeGrace(properties.getMaxConnectionAgeGraceInSeconds(), SECONDS);
        }
        if (properties.getMaxConnectionAgeInSeconds() != null) {
            builder.maxConnectionAge(properties.getMaxConnectionAgeInSeconds(), SECONDS);
        }
        if (properties.getMaxConnectionIdleInSeconds() != null) {
            builder.maxConnectionIdle(properties.getMaxConnectionIdleInSeconds(), SECONDS);
        }
        if (properties.getPermitKeepAliveWithoutCalls() != null) {
            builder.permitKeepAliveWithoutCalls(properties.getPermitKeepAliveWithoutCalls());
        }
        if (properties.getFlowControlWindow() != null) {
            builder.flowControlWindow(properties.getFlowControlWindow());
        }
        if (properties.getMaxMessageSize() != null) {
            builder.maxInboundMessageSize(properties.getMaxMessageSize());
        }
        if (properties.getMaxHeaderListSize() != null) {
            builder.maxInboundMetadataSize(properties.getMaxHeaderListSize());
        }
        if (properties.getMaxConcurrentCallsPerConnection() != null) {
            builder.maxConcurrentCallsPerConnection(properties.getMaxConcurrentCallsPerConnection());
        }
        if (properties.getSecurity().isEnabled()) {
            builder.sslContext(properties.getSecurity().createSslContext(true));
        }
        if (properties.getCompressorEncodings() != null) {
            builder.compressorRegistry(
                    GrpcCompressorUtil.createCompressorRegistry(properties.getCompressorEncodings()));
        }
        if (properties.getDecompressorEncodings() != null) {
            builder.decompressorRegistry(
                    GrpcCompressorUtil.createDecompressorRegistry(properties.getDecompressorEncodings()));
        }

        services.forEach(service -> {
            LOGGER.info(
                    "Registered gRPC service: {}, bean: {}, class: {}",
                    service.getDefinition().getServiceDescriptor().getName(),
                    service.getBeanName(),
                    service);
            builder.addService(intercept(service.getDefinition(), createServerInterceptors(properties)));
        });
        return builder.build();
    }

    private List<ServerInterceptor> createServerInterceptors(GrpcServerProperties properties) {
        List<ServerInterceptor> serverInterceptors = new LinkedList<>();
        if (grpcTracing != null) {
            serverInterceptors.add(grpcTracing.newServerInterceptor());
        }
        createSetCompressionServerInterceptor(properties).ifPresent(serverInterceptors::add);
        return ImmutableList.copyOf(serverInterceptors);
    }

    private Optional<ServerInterceptor> createSetCompressionServerInterceptor(
            GrpcServerProperties properties) {
        List<String> compressorEncodings = properties.getCompressorEncodings();
        if (compressorEncodings == null || compressorEncodings.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SetCompressionServerInterceptor(compressorEncodings));
    }

    private static final class SetCompressionServerInterceptor implements ServerInterceptor {

        private final Set<String> compressorEncodings;

        private SetCompressionServerInterceptor(Collection<String> compressorEncodings) {
            this.compressorEncodings = ImmutableSet.copyOf(compressorEncodings);
        }

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call,
                Metadata headers,
                ServerCallHandler<ReqT, RespT> next) {
            String messageAcceptEncoding = headers.get(MESSAGE_ACCEPT_ENCODING_KEY);
            if (messageAcceptEncoding != null) {
                Set<String> acceptedEncodings = ImmutableSet.copyOf(
                        ACCEPT_ENCODING_SPLITTER.split(messageAcceptEncoding));
                Sets.intersection(compressorEncodings, acceptedEncodings)
                        .stream()
                        .findFirst()
                        .ifPresent(call::setCompression);
            }
            return next.startCall(call, headers);
        }
    }
}
