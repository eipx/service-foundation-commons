package io.github.eipx.servicefoundation.commons.events.maven.plugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import io.github.eipx.servicefoundation.commons.observability.event.DocumentedEvent;

public class EventsAnnotationsRetriever {

    private final Reflections reflections;

    public EventsAnnotationsRetriever(ClassLoader compileClassLoader, List<String> packagesToScan) {
        FilterBuilder packageFilter = new FilterBuilder();

        if (packagesToScan != null) {
            packagesToScan.forEach(packageToScan -> packageFilter.includePackage(packageToScan));
        }

        reflections = new Reflections(new ConfigurationBuilder()
                                              .setUrls(ClasspathHelper.forClassLoader(compileClassLoader))
                                              .addClassLoader(compileClassLoader)
                                              .setScanners(
                                                      new SubTypesScanner(),
                                                      new FieldAnnotationsScanner()
                                              )
                                              .setExpandSuperTypes(false)
                                              .filterInputsBy(packageFilter)

        );
    }

    public Set<Field> retrieveFields(Class<DocumentedEvent> annotation) {
        return reflections.getFieldsAnnotatedWith(annotation);
    }

}
