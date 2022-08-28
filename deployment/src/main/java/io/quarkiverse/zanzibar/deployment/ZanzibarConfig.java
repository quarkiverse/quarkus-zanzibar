package io.quarkiverse.zanzibar.deployment;

import static io.quarkiverse.zanzibar.deployment.ZanzibarProcessor.FEATURE;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = FEATURE)
public class ZanzibarConfig {

    /**
     * Configuration for JAX-RS authorization filter.
     */
    @ConfigItem
    public JAXRSFilterConfig filter;

}
