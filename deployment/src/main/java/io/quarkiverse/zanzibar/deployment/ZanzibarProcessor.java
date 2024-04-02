package io.quarkiverse.zanzibar.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.zanzibar.DefaultZanzibarUserIdExtractor;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.ZanzibarUserIdExtractor;
import io.quarkiverse.zanzibar.jaxrs.ZanzibarDynamicFeature;
import io.quarkiverse.zanzibar.runtime.ZanzibarRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.resteasy.reactive.spi.DynamicFeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;

class ZanzibarProcessor {

    public static final String FEATURE = "zanzibar";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(STATIC_INIT)
    void registerProvider(
            ZanzibarConfig config,
            Capabilities capabilities,
            ZanzibarRecorder recorder,
            BuildProducer<DynamicFeatureBuildItem> dynamicFeatures,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<UnremovableBeanBuildItem> unremovableBeans,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            BuildProducer<AdditionalIndexedClassesBuildItem> additionalIndexedClasses) {

        if (!config.filter.enabled) {
            return;
        }

        RuntimeValue<ZanzibarDynamicFeature.FilterFactory> filterFactory;

        if (capabilities.isPresent(Capability.RESTEASY_REACTIVE)) {

            filterFactory = recorder.createReactiveFilterFactory();

            dynamicFeatures.produce(new DynamicFeatureBuildItem(ZanzibarDynamicFeature.class.getName(), false));

        } else if (capabilities.isPresent(Capability.RESTEASY)) {

            filterFactory = recorder.createSynchronousFilterFactory();

            Class<?> featureClass = ZanzibarDynamicFeature.class;
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(featureClass));
            additionalIndexedClasses.produce(new AdditionalIndexedClassesBuildItem(featureClass.getName()));
            reflectiveClass.produce(ReflectiveClassBuildItem.builder(featureClass).fields(true).methods(true).build());
        } else {
            return;
        }

        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(RelationshipManager.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(ZanzibarUserIdExtractor.class));

        additionalBeans.produce(
                AdditionalBeanBuildItem.builder()
                        .addBeanClass(DefaultZanzibarUserIdExtractor.class)
                        .build());

        var dynamicFeature = recorder.createDynamicFeature(config.filter.unauthenticatedUser, config.filter.timeout,
                config.filter.denyUnannotatedResourceMethods, filterFactory);

        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(ZanzibarDynamicFeature.class)
                        .unremovable()
                        .scope(ApplicationScoped.class)
                        .supplier(dynamicFeature)
                        .done());
    }

}
