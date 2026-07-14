package io.github.eipx.servicefoundation.commons.events.maven.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.github.eipx.servicefoundation.commons.events.maven.plugin.model.EventModel;
import io.github.eipx.servicefoundation.commons.events.maven.plugin.model.ServiceModel;
import io.github.eipx.servicefoundation.commons.observability.event.DocumentedEvent;
import static org.apache.maven.plugins.annotations.LifecyclePhase.*;

@Mojo(name = "generate", defaultPhase = PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class EventsAnnotationProcessor extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(readonly = true)
    private List<String> packagesToScan;

    @Parameter(readonly = true)
    private List<String> packagesToUpdateAndScan;

    @Parameter(readonly = true, required = true)
    private String product;

    @Parameter(readonly = true, required = true)
    private String serviceName;

    @Parameter(readonly = true, required = true)
    private String outputFile;

    @Parameter(readonly = true)
    private String applicationShortName;

    @Parameter(readonly = true)
    private List<String> eventTitleParams;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            ClassLoader compileClassLoader = getCompileClassLoader();
            Thread.currentThread().setContextClassLoader(compileClassLoader);

            List<EventModel> events = new ArrayList<>();

            if (packagesToScan != null && !packagesToScan.isEmpty()) {
                //Don't pass appShortName so component will not be updated in generated doc
                events.addAll(scanEvents(packagesToScan, compileClassLoader, null));
            }
            if (packagesToUpdateAndScan != null && !packagesToUpdateAndScan.isEmpty()) {
                events.addAll(scanEvents(packagesToUpdateAndScan, compileClassLoader, applicationShortName));
            }


            getLog().info("Write events YAML doc to " + outputFile);
            EventsYamlWriter eventsYamlWriter = new EventsYamlWriter();
            ServiceModel service = new ServiceModel(serviceName, events);

            new ServiceValidator().validate(service);

            eventsYamlWriter.writerYamlEvents(service, outputFile);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate documented events", e);
        }
    }

    private List<EventModel> scanEvents(List<String> packagesToScan, ClassLoader compileClassLoader, String appShortName)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        getLog().info("Retrieve events to be documented");
        EventsAnnotationsRetriever eventsAnnotationsRetriever = new EventsAnnotationsRetriever(compileClassLoader, packagesToScan);
        Set<Field> fields = eventsAnnotationsRetriever.retrieveFields(DocumentedEvent.class);
        getLog().debug("Found the following events to document " + fields);
        EventsModelMapper eventsModelMapper = new EventsModelMapper(product, appShortName, eventTitleParams);
        return eventsModelMapper.mapToModel(fields);
    }

    private ClassLoader getCompileClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        Set<URL> urls = new HashSet<>();

        List<String> elements = project.getCompileClasspathElements();

        for (String element : elements) {
            urls.add(new File(element).toURI().toURL());
        }

        return URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
    }


}
