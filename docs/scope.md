# Project scope

## Consumer dependencies

A Spring Boot consumer normally declares one runtime dependency:

```xml
<dependency>
    <groupId>io.github.eipx.servicefoundation.commons</groupId>
    <artifactId>service-foundation-commons-spring-boot-starter</artifactId>
</dependency>
```

A build that generates event documentation can also use:

```xml
<plugin>
    <groupId>io.github.eipx.servicefoundation.commons</groupId>
    <artifactId>service-foundation-commons-events-maven-plugin</artifactId>
</plugin>
```

Public classes are organized below the
`io.github.eipx.servicefoundation.commons` package root. The main areas are
`security`, `lifecycle`, `core`, `grpc.server`, and `observability`.

## Included behavior

- Ordered synchronous startup and shutdown callbacks.
- Application status and Actuator health integration.
- Named password-provider registration.
- File-backed JKS and PKCS12 key and trust stores with password clearing.
- `grpc.server.*` property binding, annotated service discovery, application and
  global interceptors, detailed status trailers, compression, Netty lifecycle,
  and TLS.
- Micrometer gRPC counters and timers.
- Event declarations, event building, CEF journal mapping, and MDC behavior.
- Activity-log fields and serialization.
- Disableable rolling and Logstash appenders with reusable plain and PKCS12 TLS
  Logback resources.
- Local Dropwizard and Micrometer metrics with rolling CSV reporting.
- Local Micrometer and Brave trace context with a no-op reporter when remote
  tracking is disabled, retaining in-process and gRPC trace propagation without
  exporting spans.
- Event YAML generation from `@DocumentedEvent` declarations.

## Deliberately omitted

The following capabilities are outside the current project scope:

- A gRPC client starter or managed channel factory.
- Hardware-backed, native, or pseudo-URI key stores and their background
  integrity checkers.
- General-purpose XML, launcher, and platform utility collections.
- Container images, archetypes, parent-project scaffolding, and consumer test
  starters.
- Managed remote metrics reporters and remote Zipkin export. Logstash appender
  resources are included, but the project does not provision or operate a
  collector.
- An environment post-processor that discovers monitoring and keystore files
  from a separate configuration directory. Consumers should use Spring's normal
  configuration locations instead.
- Generic web-server or servlet-container customization unrelated to the
  documented service runtime.

## Known validation risks

1. A consumer can override Netty modules independently of the project's
   `4.1.110.Final` baseline. Validate the resolved Netty/gRPC tree and native TLS
   path on Linux whenever an override is present.
2. Remote metrics and tracing export are not supported. Consumers that enable
   a remote Logstash appender must integration-test its endpoint, PKCS12 stores,
   reconnect behavior, and failure handling in their own environment.
3. Only file-backed JKS and PKCS12 key stores are supported. Other key-store
   providers require an independently maintained adapter.
4. Auto-configuration compatibility must be verified when another dependency
   defines beans for the same gRPC server, health, logging, or tracing concerns.
