package io.github.eipx.servicefoundation.commons.grpc.server;

import java.util.Collection;

public interface GrpcServiceDiscoverer {

    Collection<GrpcServerServiceDefinition> findGrpcServices();
}
