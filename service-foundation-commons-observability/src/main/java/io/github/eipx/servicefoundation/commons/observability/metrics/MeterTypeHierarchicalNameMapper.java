package io.github.eipx.servicefoundation.commons.observability.metrics;

import java.util.Locale;

import com.google.common.base.CharMatcher;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;

import static java.util.stream.Collectors.*;

/**
 * The default name mapper (@link HierarchicalNameMapper{@link #DEFAULT}) does not print the meter type.
 */
public final class MeterTypeHierarchicalNameMapper implements HierarchicalNameMapper {

    private static final MeterTypeHierarchicalNameMapper INSTANCE = new MeterTypeHierarchicalNameMapper();

    public static MeterTypeHierarchicalNameMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public String toHierarchicalName(Meter.Id id, NamingConvention namingConvention) {
        StringBuilder hierarchicalName = new StringBuilder();
        hierarchicalName.append(getTypeName(id.getType()));
        hierarchicalName.append('.');
        hierarchicalName.append(getName(id, namingConvention));
        if (!id.getTags().isEmpty()) {
            hierarchicalName.append('.');
            hierarchicalName.append(getTagNames(id, namingConvention));
        }
        return hierarchicalName.toString();
    }

    @Override
    public String toString() {
        return "MeterTypeHierarchicalNameMapper.INSTANCE";
    }

    private static String getTypeName(Meter.Type type) {
        return type.name().toLowerCase(Locale.ENGLISH);
    }

    private static String getName(Meter.Id id, NamingConvention namingConvention) {
        return id.getConventionName(namingConvention);
    }

    private static String getTagNames(Meter.Id id, NamingConvention namingConvention) {
        return id.getConventionTags(namingConvention)
                 .stream()
                 .map(tag -> tag.getKey() + "." + tag.getValue())
                 .map(formattedTag -> CharMatcher.whitespace().replaceFrom(formattedTag, '_'))
                 .collect(joining("."));
    }
}
