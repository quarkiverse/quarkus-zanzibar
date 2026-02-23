package io.quarkiverse.zanzibar.it;

import io.quarkiverse.zanzibar.ZanzibarPermission;

public class RestParamsPermission extends ZanzibarPermission {

    private final String pathId;
    private final String queryId;
    private final String headerId;
    private final String cookieId;
    private final String matrixId;

    public RestParamsPermission(String name, String pathId, String queryId, String headerId, String cookieId,
            String matrixId) {
        super(name);
        this.pathId = pathId;
        this.queryId = queryId;
        this.headerId = headerId;
        this.cookieId = cookieId;
        this.matrixId = matrixId;
    }

    @Override
    public String getObjectType() {
        return "thing";
    }

    @Override
    public String getObjectId() {
        return String.join("|",
                String.valueOf(pathId),
                String.valueOf(queryId),
                String.valueOf(headerId),
                String.valueOf(cookieId),
                String.valueOf(matrixId));
    }
}
