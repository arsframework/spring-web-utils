package com.arsframework.spring.web.utils.param;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;
import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

/**
 * Method processor supports {@link Rename} parameters renaming
 *
 * @author Woody
 */
public class ParameterRenamingProcessor extends ServletModelAttributeMethodProcessor {
    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    /**
     * A map caching annotation definitions of command objects
     */
    private final Map<Class<?>, Map<String, String>> classRenameMappings = new ConcurrentHashMap<>();

    public ParameterRenamingProcessor() {
        super(true);
    }

    /**
     * Convert the parameter name
     */
    private static final class ParameterDataBinder extends ExtendedServletRequestDataBinder {
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
                    propertyValues.add(v, propertyValues.getPropertyValue(k).getValue());
                }
            });
            this.renames.forEach((k, v) -> {
                if (!propertyValues.contains(k)) {
                    propertyValues.removePropertyValue(v);
                }
            });
        }
    }

    /**
     * Get the rename and field mappings
     *
     * @param clazz The target class
     * @return Rename and field mappings
     */
    private Map<String, String> getRenameMappings(Class<?> clazz) {
        if (clazz == Object.class) {
            return Collections.emptyMap();
        }
        Map<String, String> renames = this.classRenameMappings.get(clazz);
        if (renames == null) {
            renames = new HashMap<>();
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Rename rename = field.getAnnotation(Rename.class);
                String name = rename == null ? null : rename.value();
                if (name != null && !(name = name.trim()).isEmpty()
                        && renames.put(name, field.getName()) != null) {
                    throw new RuntimeException("Duplicate parameter name: " + name);
                }
            }
            renames.putAll(this.getRenameMappings(clazz.getSuperclass()));
            this.classRenameMappings.put(clazz, renames.isEmpty() ? Collections.emptyMap() : renames);
        }
        return renames;
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        Object target = binder.getTarget();
        WebBindingInitializer initializer = this.requestMappingHandlerAdapter.getWebBindingInitializer();
        if (target == null || initializer == null) {
            return;
        }
        Map<String, String> mapping = this.getRenameMappings(target.getClass());
        ParameterDataBinder parameterDataBinder = new ParameterDataBinder(target, binder.getObjectName(), mapping);
        initializer.initBinder(parameterDataBinder);
        super.bindRequestParameters(parameterDataBinder, request);
    }
}
