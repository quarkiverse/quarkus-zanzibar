package io.quarkiverse.zanzibar.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Dynamically sources the Object ID for the FGA authorization check from a request property.
 */
@Inherited
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface FGARequestObject {

    /**
     * Name of the request property to use as the Object ID.
     */
    String property();

    /**
     * Object Type for FGA authorization check.
     */
    String type();

}
