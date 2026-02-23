package io.quarkiverse.zanzibar.it;

import io.quarkiverse.zanzibar.ZanzibarPermission;

public class RestFormPermission extends ZanzibarPermission {

    private final String formId;

    public RestFormPermission(String name, String formId) {
        super(name);
        this.formId = formId;
    }

    @Override
    public String getObjectType() {
        return "thing";
    }

    @Override
    public String getObjectId() {
        return String.valueOf(formId);
    }
}
