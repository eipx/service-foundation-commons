# Service Foundation Commons

Service Foundation Commons is a focused Java 17 and Spring Boot 3 library for
services that need consistent lifecycle management, health reporting, TLS,
gRPC server integration, and local observability.

The public Java namespace and Maven group are
`io.github.eipx.servicefoundation.commons`. The initial development version is
`0.1.0-SNAPSHOT`.

## Modules

| Module | Responsibility |
| --- | --- |
| `service-foundation-commons-bom` | Consumer dependency alignment |
| `service-foundation-commons-api` | Lifecycle contracts, password providers, and the file/JKS TLS model |
| `service-foundation-commons-core-autoconfigure` | Startup and shutdown phases, status, and application health |
| `service-foundation-commons-grpc-server` | Netty gRPC server, service discovery, interceptors, TLS, metrics, and status trailers |
| `service-foundation-commons-observability` | Event journal, CEF and activity logging, Logback appenders, local metrics, and local trace context |
| `service-foundation-commons-spring-boot-starter` | Aggregate runtime dependency for consumer services |
| `service-foundation-commons-events-maven-plugin` | Event YAML generation from `@DocumentedEvent` declarations |
| `service-foundation-commons-compatibility-tests` | Checks for the supported public behavior and configuration properties |

The runtime relationship is:

```text
Consumer service
  -> service-foundation-commons-spring-boot-starter
       -> API and security
       -> lifecycle and health auto-configuration
       -> gRPC server auto-configuration
       -> logging, events, activity, metrics, and tracing auto-configuration
```

## Supported baseline

- Java 17
- Apache Maven 3.6.3 or newer
- Spring Boot 3.3.5
- gRPC 1.68.1
- Protobuf 3.25.5
- Netty 4.1.110.Final within this project

The library supports the existing `grpc.server.*`, `monitoring.*`, and
`metrics.*` configuration families, together with application identity and
key-manager or trust-manager settings. See [Project scope](docs/scope.md) for
the retained and deliberately omitted capabilities.

## Build on Linux

This workspace was prepared and statically reviewed on Windows, but it was not
built there. Run builds on a supported Linux environment with access to the
Maven repositories required by the project.

Install the complete reactor into the local Maven repository:

```bash
cd /path/to/service-foundation-commons
mvn -U -Drevision=0.1.0-SNAPSHOT clean install
```

The reactor flattens Maven's CI-friendly `${revision}` placeholder during the
install and deploy lifecycles. A separately invoked consumer build can therefore
resolve normal versioned POMs from the same local repository.

Build a consumer application after installing the library:

```bash
cd /path/to/consumer-project
mvn -U -Dservice-foundation-commons.version=0.1.0-SNAPSHOT \
  -pl application -am clean verify
```

Adjust the consumer module selector and version-property name if its reactor
uses different conventions. Run packaging and external integration modules only
in the Linux environment that supplies their infrastructure prerequisites.

Detailed preflight and behavioral checks are in
[Linux validation](docs/linux-validation.md).

## Jenkins

The included `Jenkinsfile` runs the Java 17 and Maven 3.6.3-compatible reactor
verification on a Linux agent. Jenkins must define tools named `jdk17` and
`maven-3.6.3`, or the neutral tool names can be adjusted for the installation.
The public pipeline deliberately performs no artifact deployment, release
tagging, credential binding, or environment-specific scanning.

## Packaging flattened executable JARs

Applications that use Maven Assembly to unpack all runtime dependencies into a
single JAR must merge Java service-provider descriptors. Otherwise, providers
such as gRPC name resolvers can overwrite one another according to dependency
order. See the sanitized
[Maven Assembly service-loader example](examples/maven-assembly-service-loader/README.md).

## Release model

Keep `0.1.x-SNAPSHOT` versions while compatibility is being established. Promote
to `1.0.0` only after the supported TLS, gRPC, health, activity-log, event-journal,
and metrics behavior passes in a representative staged service.

Do not place another starter that owns the same gRPC server or observability
beans on the classpath. Competing auto-configurations can create duplicate beans
or make startup ordering unpredictable.

Treat this repository as an independently versioned reusable library. Add a
capability only when it belongs in the documented scope and protect its public
behavior with compatibility tests.
