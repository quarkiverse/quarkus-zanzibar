package io.quarkiverse.zanzibar;

import java.util.Objects;

/**
 * Models a single relationship managed by FGA backend.
 */
public class Relationship {

    private final String objectType;
    private final String objectId;
    private final String relation;
    private final String user;

    public Relationship(String objectType, String objectId, String relation, String user) {
        this.objectType = objectType;
        this.objectId = objectId;
        this.relation = relation;
        this.user = user;
    }

    public static Relationship of(String objectType, String objectId, String relation, String user) {
        return new Relationship(objectType, objectId, relation, user);
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
     * User that relates to Object ID.
     */
    public String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Relationship))
            return false;
        Relationship that = (Relationship) o;
        return Objects.equals(objectType, that.objectType) && Objects.equals(objectId, that.objectId)
                && Objects.equals(relation, that.relation) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, objectId, relation, user);
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "objectType='" + objectType + '\'' +
                ", objectId='" + objectId + '\'' +
                ", relation='" + relation + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
