package com.rezwan.hyperpay_android.msa;

import androidx.annotation.NonNull;


class RequestData {
    private final ServerMode serverMode;
    private final String authorizationBearer;
    private final String entityId;

    RequestData(@NonNull ServerMode serverMode, @NonNull String authorizationBearer, @NonNull String entityId) {
        this.serverMode = serverMode;
        this.authorizationBearer = authorizationBearer;
        this.entityId = entityId;
    }

    ServerMode getServerMode() {
        return this.serverMode;
    }

    String getAuthorizationBearer() {
        return this.authorizationBearer;
    }

    String getEntityId() {
        return this.entityId;
    }
}
