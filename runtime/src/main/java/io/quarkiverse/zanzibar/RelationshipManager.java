package io.quarkiverse.zanzibar;

import java.util.List;

import io.smallrye.mutiny.Uni;

public interface RelationshipManager {

    Uni<Boolean> check(Relationship relationship);

    Uni<Void> add(List<Relationship> relationships);

    Uni<Void> remove(List<Relationship> relationships);

}
