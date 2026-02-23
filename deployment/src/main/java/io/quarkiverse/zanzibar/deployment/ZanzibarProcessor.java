package io.quarkiverse.zanzibar.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.quarkiverse.zanzibar.ContextAwareRelationshipManager;
import io.quarkiverse.zanzibar.DefaultUserExtractor;
import io.quarkiverse.zanzibar.RelationshipContext;
import io.quarkiverse.zanzibar.RelationshipContextManager;
import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkiverse.zanzibar.UserExtractor;
import io.quarkiverse.zanzibar.ZanzibarPermission;
import io.quarkiverse.zanzibar.ZanzibarPermissionChecker;
import io.quarkiverse.zanzibar.ZanzibarPermissionIdentityAugmentor;
import io.quarkiverse.zanzibar.runtime.ZanzibarRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.StringPermission;

class ZanzibarProcessor {

    public static final String FEATURE = "zanzibar";

    private static final DotName PERMISSIONS_ALLOWED = DotName.createSimple(PermissionsAllowed.class.getName());
    private static final DotName PERMISSIONS_ALLOWED_LIST = DotName.createSimple(PermissionsAllowed.List.class.getName());
    private static final DotName ZANZIBAR_PERMISSION = DotName.createSimple(ZanzibarPermission.class.getName());
    private static final DotName STRING_PERMISSION = DotName.createSimple(StringPermission.class.getName());
    private static final String PERMISSION_ATTR = "permission";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(STATIC_INIT)
    void registerProvider(
            ZanzibarConfig config,
            ZanzibarRecorder recorder,
            CombinedIndexBuildItem combinedIndex,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<UnremovableBeanBuildItem> unremovableBeans) {
        validatePermissions(combinedIndex.getIndex());
        Map<String, String> defaultUserTypeMappings = buildDefaultUserTypes(combinedIndex.getIndex(), config);

        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(RelationshipManager.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(ContextAwareRelationshipManager.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(RelationshipContextManager.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(RelationshipContext.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(UserExtractor.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(ZanzibarPermissionChecker.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(ZanzibarPermissionIdentityAugmentor.class));
        unremovableBeans.produce(
                UnremovableBeanBuildItem.beanTypes(io.quarkiverse.zanzibar.DefaultUserTypeResolver.class));

        additionalBeans.produce(
                AdditionalBeanBuildItem.builder()
                        .addBeanClass(RelationshipContextManager.class)
                        .addBeanClass(RelationshipContext.class)
                        .addBeanClass(ZanzibarPermissionChecker.class)
                        .addBeanClass(ZanzibarPermissionIdentityAugmentor.class)
                        .build());

        var defaultUserExtractor = recorder.createUserExtractor(config.extractUserTypeFromName(),
                config.userTypeSeparator(), config.extractUserTypeFromRoles());
        var defaultUserTypeResolver = recorder.createDefaultUserTypeResolver(defaultUserTypeMappings);

        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(DefaultUserExtractor.class)
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .types(UserExtractor.class, DefaultUserExtractor.class)
                        .supplier(defaultUserExtractor)
                        .done());
        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(io.quarkiverse.zanzibar.DefaultUserTypeResolver.class)
                        .scope(ApplicationScoped.class)
                        .types(io.quarkiverse.zanzibar.DefaultUserTypeResolver.class)
                        .supplier(defaultUserTypeResolver)
                        .done());
    }

    private static void validatePermissions(IndexView index) {
        for (AnnotationInstance instance : index.getAnnotations(PERMISSIONS_ALLOWED)) {
            validatePermissionAnnotation(instance, index);
        }
        for (AnnotationInstance instance : index.getAnnotations(PERMISSIONS_ALLOWED_LIST)) {
            AnnotationValue value = instance.value();
            if (value == null) {
                continue;
            }
            for (AnnotationInstance nested : value.asNestedArray()) {
                validatePermissionAnnotation(nested, index);
            }
        }
    }

    private static void validatePermissionAnnotation(AnnotationInstance instance, IndexView index) {
        AnnotationValue permissionValue = instance.value(PERMISSION_ATTR);
        if (permissionValue == null) {
            return;
        }
        DotName permissionClass = permissionValue.asClass().name();
        if (permissionClass.equals(STRING_PERMISSION)) {
            return;
        }
        ClassInfo permissionInfo = index.getClassByName(permissionClass);
        if (permissionInfo == null || !isAssignableFrom(permissionInfo, ZANZIBAR_PERMISSION, index)) {
            AnnotationTarget target = instance.target();
            String location = target == null ? "<unknown>" : target.toString();
            throw new IllegalStateException("PermissionsAllowed permission class " + permissionClass + " used at " + location
                    + " must extend " + ZANZIBAR_PERMISSION);
        }
    }

    private static Map<String, String> buildDefaultUserTypes(IndexView index, ZanzibarConfig config) {
        Map<String, String> defaults = new java.util.HashMap<>();
        Map<String, String> sources = new java.util.HashMap<>();

        for (AnnotationInstance instance : index.getAnnotations(PERMISSIONS_ALLOWED)) {
            addDefaultUserType(instance, instance.target(), config, defaults, sources);
        }
        for (AnnotationInstance instance : index.getAnnotations(PERMISSIONS_ALLOWED_LIST)) {
            AnnotationValue value = instance.value();
            if (value == null) {
                continue;
            }
            AnnotationTarget target = instance.target();
            for (AnnotationInstance nested : value.asNestedArray()) {
                addDefaultUserType(nested, target, config, defaults, sources);
            }
        }

        return defaults;
    }

    private static void addDefaultUserType(AnnotationInstance instance, AnnotationTarget targetOverride, ZanzibarConfig config,
            Map<String, String> defaults, Map<String, String> sources) {
        AnnotationValue permissionValue = instance.value(PERMISSION_ATTR);
        if (permissionValue == null) {
            return;
        }
        DotName permissionClass = permissionValue.asClass().name();
        if (permissionClass.equals(STRING_PERMISSION)) {
            return;
        }

        String resourceClass = resolveResourceClassName(targetOverride != null ? targetOverride : instance.target());
        if (resourceClass == null) {
            return;
        }

        String resolved = resolveDefaultUserType(config, resourceClass);
        if (resolved == null) {
            return;
        }

        String permissionName = permissionClass.toString();
        String existing = defaults.get(permissionName);
        if (existing != null && !existing.equals(resolved)) {
            String existingSource = sources.get(permissionName);
            throw new IllegalStateException("Permission class " + permissionName + " has conflicting default user types: '"
                    + existing + "' from " + existingSource + " and '" + resolved + "' from " + resourceClass);
        }
        defaults.put(permissionName, resolved);
        sources.put(permissionName, resourceClass);
    }

    private static String resolveResourceClassName(AnnotationTarget target) {
        if (target == null) {
            return null;
        }
        return switch (target.kind()) {
            case CLASS -> target.asClass().name().toString();
            case METHOD -> target.asMethod().declaringClass().name().toString();
            case FIELD -> target.asField().declaringClass().name().toString();
            default -> null;
        };
    }

    private static String resolveDefaultUserType(ZanzibarConfig config, String resourceClass) {
        ZanzibarConfig.DefaultUserType defaults = config.defaultUserTypes().get(resourceClass);
        if (defaults == null) {
            return null;
        }
        String resolved = defaults.userType();
        if (resolved == null || resolved.isBlank()) {
            return null;
        }
        return resolved;
    }

    private static boolean isAssignableFrom(ClassInfo clazz, DotName target, IndexView index) {
        if (clazz == null) {
            return false;
        }
        if (clazz.name().equals(target)) {
            return true;
        }
        DotName superName = clazz.superName();
        if (superName != null) {
            if (superName.equals(target)) {
                return true;
            }
            if (isAssignableFrom(index.getClassByName(superName), target, index)) {
                return true;
            }
        }
        for (DotName iface : clazz.interfaceNames()) {
            if (iface.equals(target)) {
                return true;
            }
            if (isAssignableFrom(index.getClassByName(iface), target, index)) {
                return true;
            }
        }
        return false;
    }

}
