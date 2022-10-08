package io.quarkiverse.zanzibar.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Provides the relation to use in the FGA authorization check.
 * <p>
 * To allow any relation to pass the authorization check the {@link #ANY} value can be used.
 */
@Inherited
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface FGARelation {

    String ANY = "*";

    /**
     * Name of relation to use for authorization check.
     */
    String value();
}
