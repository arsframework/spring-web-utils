package com.arsframework.spring.web.utils.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rename request parameter
 *
 * @author Woody
 * @date 2020/11/10
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rename {
    /**
     * The name of the request parameter to bind to.
     */
    String value();
}
