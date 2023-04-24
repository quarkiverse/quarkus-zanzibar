package io.quarkiverse.zanzibar.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Provides a user type to build user objects for checking.
 * <br>
 * Implementations such as OpenFGA require users to be objects. For example a user with id "1" that is of type
 * "user" it must be presented as "user:1"; this annotation provides the user type name.
 */
@Inherited
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface FGAUserType {

    /**
     * Name of the user objec type.
     */
    String value();

}
