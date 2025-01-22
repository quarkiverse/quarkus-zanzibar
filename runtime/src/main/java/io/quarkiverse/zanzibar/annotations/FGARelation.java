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
 * To allow any relation to pass the authorization check the {@link #ANY} value can be used. This is useful to
 * include the authorization filter in the chain but skip the check. Alternatively, the {@link FGAIgnore} annotation
 * can be used to exclude the method or class from the authorization filter.
 *
 * @see FGAIgnore
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
