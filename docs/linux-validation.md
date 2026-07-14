# Linux build and validation

## Prerequisites

- JDK 17 selected by `JAVA_HOME`.
- Maven 3.6.3 or newer.
- Access and credentials for every Maven repository configured by the build.
- The library and consumer project on the same Linux host, or a shared snapshot
  repository available to both builds.
- The consumer's normal databases, certificates, configuration, and integration
  test infrastructure for anything beyond unit tests.

Do not use a Windows build as release evidence. The project was prepared and
statically checked on Windows, but its supported build and runtime validation
belong on Linux.

## 1. Install Service Foundation Commons locally

```bash
cd /path/to/service-foundation-commons
mvn -U -Drevision=0.1.0-SNAPSHOT clean install
```

Confirm that the parent, BOM, runtime modules, aggregate starter, and Maven plugin
were installed under the local repository path for
`io.github.eipx.servicefoundation.commons`.

## 2. Inspect the consumer dependency tree

From the consumer project, capture the resolved runtime dependencies:

```bash
cd /path/to/consumer-project
mvn -Dservice-foundation-commons.version=0.1.0-SNAPSHOT \
  -pl application -am dependency:tree -Dverbose \
  > target/service-foundation-dependency-tree.txt
```

Confirm that the aggregate starter resolves from the locally installed version,
that no superseded framework starters remain, and that each required dependency
has a single intended provider.

Inspect Netty and gRPC convergence explicitly:

```bash
mvn -Dservice-foundation-commons.version=0.1.0-SNAPSHOT \
  -pl application dependency:tree -Dverbose \
  -Dincludes=io.netty:*,io.grpc:*
```

Do not promote a release if mixed Netty lines produce linkage warnings or
TLS/gRPC failures. Record the complete dependency tree used by the passing
release candidate.

## 3. Build the consumer application slice

```bash
mvn -U -Dservice-foundation-commons.version=0.1.0-SNAPSHOT \
  -pl application -am clean verify
```

Replace `application` with the actual consumer module path. This targeted build
should include the consumer's parent and generated-source modules while avoiding
unrelated packaging or external integration modules.

## 4. Run the complete supported-Linux build

Run the consumer's normal full-reactor command in the Linux environment that
supplies all packaging and integration-test prerequisites:

```bash
mvn -U -Dservice-foundation-commons.version=0.1.0-SNAPSHOT clean install
```

## 5. Behavioral acceptance gates

Complete all applicable checks before publishing `1.0.0`:

1. Start the consumer with the same Spring configuration locations and profiles
   used by its release launcher.
2. Complete mutual-TLS negotiation with representative file-backed JKS or PKCS12
   key and trust stores.
3. Invoke every exposed gRPC method, including custom metadata interceptors, and
   compare status and trailer behavior for both success and failure responses.
4. Verify Actuator application health during startup, normal operation, and
   shutdown.
5. Compare application, event-journal, and activity-log field formats with an
   approved behavioral baseline.
6. Verify local rolling CSV metrics are created and rotate as expected.
7. Generate and inspect `activities.yml` with the event Maven plugin.
8. With remote reporting disabled, confirm that no connection is attempted to
   remote Logstash, metrics, or Zipkin collectors.
9. Confirm that startup and shutdown callbacks execute once and in their
   configured order.

Keep representative logs, gRPC responses, metrics files, and generated event
metadata with the release evidence. A successful Maven build alone does not
establish behavioral compatibility.
