package io.quarkiverse.zanzibar.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Provides a static Object ID for the FGA authorization check.
 */
@Inherited
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface FGAObject {

    /**
     * Object ID for FGA authorization check..
     */
    String id();

    /**
     * Object Type for FGA authorization check.
     */
    String type();

}
