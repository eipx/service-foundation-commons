# Maven Assembly service-loader example

Maven Assembly's built-in `jar-with-dependencies` descriptor unpacks runtime
dependencies into one archive. When several dependencies contain the same
`META-INF/services/<interface>` path, a plain unpack can retain only one file.
That makes Java `ServiceLoader` behavior depend on archive ordering.

For example, different gRPC dependencies can publish name-resolver providers in
the same `META-INF/services/io.grpc.NameResolverProvider` path. Losing the DNS
provider can make an ordinary host and port fail before a connection is opened.

Copy [`jar-with-dependencies.xml`](jar-with-dependencies.xml) into the consumer
module as `src/assembly/jar-with-dependencies.xml`, then reference it from the
Maven Assembly Plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
            <configuration>
                <descriptors>
                    <descriptor>src/assembly/jar-with-dependencies.xml</descriptor>
                </descriptors>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The descriptor's `metaInf-services` handler appends provider declarations
instead of allowing later dependencies to overwrite earlier ones.

After packaging a gRPC application, inspect the merged resolver declaration:

```bash
unzip -p target/application-jar-with-dependencies.jar \
  META-INF/services/io.grpc.NameResolverProvider
```

For a TCP client, the result must include
`io.grpc.internal.DnsNameResolverProvider`. Additional providers, including a
Unix-domain-socket provider, can be present alongside it.

Spring Boot executable JARs normally keep dependencies as nested JARs and do not
need this Assembly-specific configuration.
