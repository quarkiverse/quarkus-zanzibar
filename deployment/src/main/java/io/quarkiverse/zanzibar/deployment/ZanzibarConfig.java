package io.quarkiverse.zanzibar.deployment;

import static io.quarkiverse.zanzibar.deployment.ZanzibarProcessor.FEATURE;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigRoot
@ConfigMapping(prefix = ZanzibarConfig.PREFIX)
public interface ZanzibarConfig {

    String PREFIX = "quarkus." + FEATURE;

    /**
     * Configuration for JAX-RS authorization filter.
     */
    JAXRSFilterConfig filter();

}
