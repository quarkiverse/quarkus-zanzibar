package io.quarkiverse.zanzibar.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;

@Inherited
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface FGADynamicObject {

    enum Source {
        PATH,
        QUERY,
        HEADER,
        REQUEST
    }

    Source source();

    String sourceProperty();

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
