package io.github.eipx.servicefoundation.commons.events.maven.plugin;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.eipx.servicefoundation.commons.events.maven.plugin.model.ServiceModel;

import static java.nio.charset.StandardCharsets.*;

public class EventsYamlWriter {

    public void writerYamlEvents(ServiceModel service, String outputFile) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        Path outputPath = Paths.get(outputFile);
        Files.createDirectories(outputPath.getParent());
        try (Writer writer = Files.newBufferedWriter(outputPath, UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, service);
        }
    }
}
