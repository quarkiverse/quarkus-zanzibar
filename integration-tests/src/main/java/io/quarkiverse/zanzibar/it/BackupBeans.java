package io.quarkiverse.zanzibar.it;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import io.quarkiverse.zanzibar.DefaultZanzibarUserIdExtractor;
import io.quarkiverse.zanzibar.ZanzibarUserIdExtractor;
import io.quarkus.arc.DefaultBean;

@Dependent
public class BackupBeans {

    @Produces
    @DefaultBean
    public ZanzibarUserIdExtractor configuration() {
        return new DefaultZanzibarUserIdExtractor();
    }

}
