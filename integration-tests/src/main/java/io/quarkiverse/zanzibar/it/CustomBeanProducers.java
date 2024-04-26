package io.quarkiverse.zanzibar.it;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import io.quarkiverse.zanzibar.DefaultUserIdExtractor;
import io.quarkiverse.zanzibar.UserIdExtractor;

@Dependent
public class CustomBeanProducers {

    @Produces
    public UserIdExtractor configuration() {
        return new DefaultUserIdExtractor();
    }

}
