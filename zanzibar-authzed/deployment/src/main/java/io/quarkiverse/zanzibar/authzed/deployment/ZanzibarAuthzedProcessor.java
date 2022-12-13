package io.quarkiverse.zanzibar.authzed.deployment;

import io.quarkiverse.zanzibar.authzed.ZanzibarAuthzedRelationshipManager;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ZanzibarAuthzedProcessor {

    private static final String FEATURE = "zanzibar-authzed";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerAuthorizer(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(
                AdditionalBeanBuildItem.builder()
                        .addBeanClass(ZanzibarAuthzedRelationshipManager.class)
                        .build());
    }

}
