package io.quarkiverse.zanzibar;

import java.util.Objects;

/**
 * Models a single relationship managed by FGA backend.
 *
 * @param objectType Type of object.
 * @param objectId ID of object.
 * @param relation Relation of Object to User.
 * @param userType Type of user.
 * @param userId ID of user.
 */
public record Relationship(String objectType, String objectId, String relation, String userType, String userId) {

    public Relationship {
        Objects.requireNonNull(objectType, "objectType cannot be null");
        Objects.requireNonNull(objectId, "objectId cannot be null");
        Objects.requireNonNull(relation, "relation cannot be null");
        Objects.requireNonNull(userType, "userType cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
    }

    public static Relationship of(String objectType, String objectId, String relation, String userType, String userId) {
        return new Relationship(objectType, objectId, relation, userType, userId);
    }

}
