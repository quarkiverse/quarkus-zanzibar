package io.quarkiverse.zanzibar.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Dynamically sources the Object ID for the FGA authorization check from a path parameter.
 */
@Inherited
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface FGAPathObject {

    /**
     * Name of the path parameter to use as the Object ID.
     */
    String param();

    /**
     * Object Type for FGA authorization check.
     */
    String type();

}
