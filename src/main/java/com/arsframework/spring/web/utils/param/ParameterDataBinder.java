package com.arsframework.spring.web.utils.param;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

/**
 * Convert the parameter name
 *
 * @author Woody
 */
public class ParameterDataBinder extends ExtendedServletRequestDataBinder {
    /**
     * The old and new name mappings
     */
    private final Map<String, String> renames;

    public ParameterDataBinder(Object target, String objectName, Map<String, String> renames) {
        super(target, objectName);
        this.renames = renames == null ? Collections.emptyMap() : renames;
    }

    @Override
    protected void addBindValues(MutablePropertyValues propertyValues, ServletRequest request) {
        super.addBindValues(propertyValues, request);
        this.renames.forEach((k, v) -> {
            if (propertyValues.contains(k)) {
                propertyValues.add(v, Objects.requireNonNull(propertyValues.getPropertyValue(k)).getValue());
            }
        });
        this.renames.forEach((k, v) -> {
            if (!propertyValues.contains(k)) {
                propertyValues.removePropertyValue(v);
            }
        });
    }
}
