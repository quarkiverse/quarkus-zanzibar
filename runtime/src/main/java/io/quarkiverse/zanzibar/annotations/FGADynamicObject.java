package io.quarkiverse.zanzibar.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Determines the dynamic source of the Object ID parameter for the FGA authorization check.
 * <p>
 * The supported sources are:
 * <ul>
 * <li>{@link Source#PATH PATH}</li>
 * <ul>
 * <li>{@link #source} - Object ID is sourced from a path parameter</li>
 * <li>{@link #sourceProperty} - Name of the path parameter</li>
 * </ul>
 * <li>{@link Source#QUERY QUERY}</li>
 * <ul>
 * <li>{@link #source} - Object ID is sourced from a query parameter</li>
 * <li>{@link #sourceProperty} - Name of the query parameter</li>
 * </ul>
 * <li>{@link Source#HEADER HEADER}</li>
 * <ul>
 * <li>{@link #source} - Object ID is sourced from a request header</li>
 * <li>{@link #sourceProperty} - Name of the header</li>
 * </ul>
 * <li>{@link Source#REQUEST REQUEST}</li>
 * <ul>
 * <li>{@link #source} - Object ID is sourced from a request property</li>
 * <li>{@link #sourceProperty} - Name of the request property</li>
 * </ul>
 * </ul>
 * <p>
 * The {@link Source#REQUEST REQUEST} source allows selecting any property set on the incoming request (i.e. from the
 * {@link javax.ws.rs.container.ContainerRequestContext#getProperty(String)}). Other filters that are run before the
 * Zanzibar authorization filter can contribute custom properties that can then be used as the source for the object
 * id.
 * <p>
 * This annotation allows selecting the source from any of the sources supported by the Zanzibar extension. For each
 * of the available sources there is a dedicated annotation that provides a less verbose usage. Additionally, if you
 * do not need a dynamic object, the {@link FGAObject} annotation allows you to provide a constant value for the object
 * id.
 * <p>
 *
 * @see FGAPathObject
 * @see FGAQueryObject
 * @see FGAHeaderObject
 * @see FGARequestObject
 * @see FGAObject
 */
@Inherited
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface FGADynamicObject {

    /**
     * Dynamic source of the Object ID for FGA authorization check.
     */
    enum Source {
        /**
         * Object ID is sourced from a path parameter.
         */
        PATH,
        /**
         * Object ID is sourced from a query parameter.
         */
        QUERY,
        /**
         * Object ID is sourced from request headers.
         */
        HEADER,
        /**
         * Object ID is sourced from a request property.
         */
        REQUEST
    }

    /**
     * Dynamic source of the Object ID for FGA authorization check.
     */
    Source source();

    /**
     * Property or parameter name relative to the {@link #source}.
     */
    String sourceProperty();

    /**
     * Object Type to use for the FGA authorization check.
     */
    String type();

    class Literal extends AnnotationLiteral<FGADynamicObject> implements FGADynamicObject {
        private final Source source;
        private final String sourceProperty;
        private final String type;

        public Literal(Source source, String sourceProperty, String type) {
            this.source = source;
            this.sourceProperty = sourceProperty;
            this.type = type;
        }

        public Source source() {
            return source;
        }

        public String sourceProperty() {
            return sourceProperty;
        }

        public String type() {
            return type;
        }
    }

}
