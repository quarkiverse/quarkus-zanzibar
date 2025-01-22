package io.quarkiverse.zanzibar;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@RequestScoped
public class RelationshipContextManager {

    record Context(Optional<String> objectType, Optional<String> objectId, Optional<String> relation,
            Optional<String> userType, Optional<String> userId) {

        public Context(Relationship relationship) {
            this(Optional.of(relationship.objectType()), Optional.of(relationship.objectId()),
                    Optional.of(relationship.relation()), Optional.of(relationship.userType()),
                    Optional.of(relationship.userId()));
        }
    }

    AtomicReference<Optional<Context>> context = new AtomicReference<>(Optional.empty());

    public void initialize(Optional<String> objectType, Optional<String> objectId, Optional<String> relation,
            Optional<String> userType, Optional<String> userId) {
        if (context.get().isPresent()) {
            throw new IllegalStateException("Relationship context already initialized");
        }
        context.set(Optional.of(new Context(objectType, objectId, relation, userType, userId)));
    }

    public void initialize(Relationship relationship) {
        if (context.get().isPresent()) {
            throw new IllegalStateException("Relationship context already initialized");
        }
        context.set(Optional.of(new Context(relationship)));
    }

    class ContextAccessor implements RelationshipContext {
        private Context getContext() {
            return context.get().orElseThrow(() -> new IllegalStateException("Relationship context not initialized"));
        }

        @Override
        public Optional<String> objectId() {
            return getContext().objectId();
        }

        @Override
        public Optional<String> objectType() {
            return getContext().objectType();
        }

        @Override
        public Optional<String> relation() {
            return getContext().relation();
        }

        @Override
        public Optional<String> userId() {
            return getContext().userId();
        }

        @Override
        public Optional<String> userType() {
            return getContext().userType();
        }
    }

    @Produces
    @RequestScoped
    public RelationshipContext produceRelationshipContext() {
        return new ContextAccessor();
    }

}
