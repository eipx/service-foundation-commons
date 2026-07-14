package io.github.eipx.servicefoundation.commons.observability.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import io.github.eipx.servicefoundation.commons.observability.metrics.reporter.RollingCsvReporterFactory;
import io.github.eipx.servicefoundation.commons.observability.metrics.reporter.RollingCsvReporterHealthIndicator;

public class MetricHealthIndicatorRegisterer implements BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricHealthIndicatorRegisterer.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        if (!(beanFactory instanceof BeanDefinitionRegistry)) {
            LOGGER.info("Bean factory is not an instance of BeanDefinitionRegistry, there will be no metric health indicators");
            return;
        }
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        String[] beanNamesForCsvReporter = beanFactory.getBeanNamesForType(RollingCsvReporterFactory.class);
        if (beanNamesForCsvReporter.length != 0) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(RollingCsvReporterHealthIndicator.class);
            registry.registerBeanDefinition("csvMetricReporter", beanDefinition);
            LOGGER.info("Registered health indicator for CSV metric reporter");
        }
    }
}
