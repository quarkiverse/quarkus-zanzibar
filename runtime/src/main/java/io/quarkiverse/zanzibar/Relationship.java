package io.quarkiverse.zanzibar;

import java.util.Objects;

/**
 * Models a single relationship managed by FGA backend.
 */
public class Relationship {

    private final String objectType;
    private final String objectId;
    private final String relation;
    private final String userId;

    public Relationship(String objectType, String objectId, String relation, String userId) {
        this.objectType = objectType;
        this.objectId = objectId;
        this.relation = relation;
        this.userId = userId;
    }

    public static Relationship of(String objectType, String objectId, String relation, String userId) {
        return new Relationship(objectType, objectId, relation, userId);
    }

    /**
     * Type of object.
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * ID of object.
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Relation of Object ID to User ID.
     */
    public String getRelation() {
        return relation;
    }

    /**
     * User ID that relates to Object ID.
     */
    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Relationship))
            return false;
        Relationship that = (Relationship) o;
        return Objects.equals(objectType, that.objectType) && Objects.equals(objectId, that.objectId)
                && Objects.equals(relation, that.relation) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, objectId, relation, userId);
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "objectType='" + objectType + '\'' +
                ", objectId='" + objectId + '\'' +
                ", relation='" + relation + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
