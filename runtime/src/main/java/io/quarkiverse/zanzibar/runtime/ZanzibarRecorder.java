package io.quarkiverse.zanzibar.runtime;

import java.util.function.Supplier;

import io.quarkiverse.zanzibar.DefaultUserExtractor;
import io.quarkiverse.zanzibar.DefaultUserTypeResolver;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ZanzibarRecorder {

    public Supplier<DefaultUserExtractor> createUserExtractor(boolean extractUserTypeFromName, String userTypeSeparator,
            boolean extractUserTypeFromRoles) {
        return () -> new DefaultUserExtractor(extractUserTypeFromName, userTypeSeparator, extractUserTypeFromRoles);
    }

    public Supplier<DefaultUserTypeResolver> createDefaultUserTypeResolver(java.util.Map<String, String> defaults) {
        return () -> new DefaultUserTypeResolver(defaults);
    }
}
