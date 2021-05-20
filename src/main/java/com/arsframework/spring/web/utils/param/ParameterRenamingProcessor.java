package com.arsframework.spring.web.utils.param;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

/**
 * Method processor supports {@link Rename} parameters renaming
 *
 * @author Woody
 */
public class ParameterRenamingProcessor extends ServletModelAttributeMethodProcessor {
    @Resource
    private ApplicationContext applicationContext;

    /**
     * A map caching annotation definitions of command objects
     */
    private final Map<Class<?>, Map<String, String>> classRenameMappings = new ConcurrentHashMap<>();

    public ParameterRenamingProcessor() {
        super(true);
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
                    throw new IllegalStateException("Duplicate parameter name: " + name);
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
        if (target == null) {
            return;
        }
        RequestMappingHandlerAdapter requestMappingHandlerAdapter =
                this.applicationContext.getBean(RequestMappingHandlerAdapter.class);
        WebBindingInitializer initializer = requestMappingHandlerAdapter.getWebBindingInitializer();
        if (initializer == null) {
            return;
        }
        Map<String, String> mapping = this.getRenameMappings(target.getClass());
        ParameterDataBinder parameterDataBinder = new ParameterDataBinder(target, binder.getObjectName(), mapping);
        initializer.initBinder(parameterDataBinder);
        super.bindRequestParameters(parameterDataBinder, request);
    }
}
