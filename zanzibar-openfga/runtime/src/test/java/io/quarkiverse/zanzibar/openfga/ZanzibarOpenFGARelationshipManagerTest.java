package io.quarkiverse.zanzibar.openfga;

import static io.quarkiverse.openfga.client.model.WriteConflictBehavior.IGNORE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import io.quarkiverse.openfga.client.AuthorizationModelClient;
import io.quarkiverse.openfga.client.AuthorizationModelClient.WriteOptions;
import io.quarkiverse.openfga.client.model.RelObject;
import io.quarkiverse.openfga.client.model.RelTupleDefinition;
import io.quarkiverse.openfga.client.model.RelTupleKey;
import io.quarkiverse.openfga.client.model.RelTupleKeyed;
import io.quarkiverse.openfga.client.model.RelUser;
import io.quarkiverse.zanzibar.Relationship;
import io.smallrye.mutiny.Uni;

class ZanzibarOpenFGARelationshipManagerTest {

    private static final Relationship RELATIONSHIP = Relationship.of("document", "report", "reader", "user", "alice");

    @Test
    void addIgnoresDuplicateTupleConflicts() {
        var client = new RecordingAuthorizationModelClient();
        var manager = new ZanzibarOpenFGARelationshipManager(client, (OpenFGAContextSupplier) null);
        var tuple = RelTupleDefinition.builder()
                .object(RelObject.of("document", "report"))
                .relation("reader")
                .user(RelUser.of("user", "alice"))
                .build();
        var options = WriteOptions.withOnDuplicate(IGNORE);

        manager.add(List.of(RELATIONSHIP)).await().indefinitely();

        assertEquals(List.of(tuple), client.writes);
        assertEquals(List.of(), client.deletes);
        assertEquals(options, client.options);
    }

    @Test
    void removeIgnoresMissingTupleConflicts() {
        var client = new RecordingAuthorizationModelClient();
        var manager = new ZanzibarOpenFGARelationshipManager(client, (OpenFGAContextSupplier) null);
        var tuple = RelTupleKey.builder()
                .object(RelObject.of("document", "report"))
                .relation("reader")
                .user(RelUser.of("user", "alice"))
                .build();
        var options = WriteOptions.withOnMissing(IGNORE);

        manager.remove(List.of(RELATIONSHIP)).await().indefinitely();

        assertEquals(List.of(), client.writes);
        assertEquals(List.of(tuple), client.deletes);
        assertEquals(options, client.options);
    }

    private static final class RecordingAuthorizationModelClient extends AuthorizationModelClient {

        private Collection<RelTupleDefinition> writes = List.of();
        private Collection<? extends RelTupleKeyed> deletes = List.of();
        private WriteOptions options;

        private RecordingAuthorizationModelClient() {
            super(null, null);
        }

        @Override
        public Uni<Map<String, Object>> write(Collection<RelTupleDefinition> writes, WriteOptions options) {
            this.writes = writes;
            this.options = options;
            return successfulWrite();
        }

        @Override
        public Uni<Map<String, Object>> write(@Nullable Collection<RelTupleDefinition> writes,
                @Nullable Collection<? extends RelTupleKeyed> deletes, WriteOptions options) {
            this.writes = writes == null ? List.of() : writes;
            this.deletes = deletes == null ? List.of() : deletes;
            this.options = options;
            return successfulWrite();
        }

        private static Uni<Map<String, Object>> successfulWrite() {
            return Uni.createFrom().item(Map.of());
        }
    }
}
