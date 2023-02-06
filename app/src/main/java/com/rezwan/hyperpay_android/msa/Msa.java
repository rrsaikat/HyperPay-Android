package com.rezwan.hyperpay_android.msa;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Msa {
    private static final String DEFAULT_TEST_ENTITY_ID = "ff80808138516ef4013852936ec200f2";
    private static final String DEFAULT_TEST_USER_ID = "ff80808138516ef4013852936ec500f6";
    private static final String DEFAULT_TEST_PASSWORD = "8XJXcsjM";
    private static final Map<String, String> REGISTRATIONS = new HashMap();
    private static final Map<String, RequestData> REQUEST_DATA_CACHE;

    public Msa() {
    }

    public static void requestCheckoutId(@NonNull String amount, @NonNull String currency, @Nullable String paymentType, @NonNull ServerMode serverMode, @NonNull String authorization, @Nullable Map<String, String> extraParameters, @NonNull CheckoutIdListener listener) {
        (new Thread(() -> {
            String checkoutId = null;
            String error = null;

            try {
                boolean sendRegistrations = extraParameters != null && Boolean.parseBoolean((String) extraParameters.remove("sendRegistrations"));
                Map<String, String> parameters = prepareParameters(amount, currency, paymentType, extraParameters, sendRegistrations);

                String authorizationBearer = authorization;
                if (authorization.isEmpty()) {
                    authorizationBearer = getAuthorizationBearer((String) parameters.remove("authentication.userId"), (String) parameters.remove("authentication.password"));
                }


                checkoutId = parseCheckoutId(HttpUtils.sendCheckoutIdRequest(serverMode, authorizationBearer, parameters));
                String entityId = (String) parameters.get("entityId");
                if (checkoutId != null && entityId != null) {
                    putRequestDataToCache(checkoutId, serverMode, authorizationBearer, entityId);
                }
            } catch (Exception var12) {
                error = var12.getMessage();
            }

            listener.onResult(checkoutId, error);
        })).start();
    }

    public static void requestPaymentStatus(@NonNull String resourcePath, @NonNull PaymentStatusListener listener) {
        (new Thread(() -> {
            String checkoutId = getRelatedCheckoutId(resourcePath);
            RequestData requestData = checkoutId != null ? (RequestData) REQUEST_DATA_CACHE.get(checkoutId) : null;
            if (requestData == null) {
                listener.onResult(false, "No related checkout id request found.");
            } else {
                boolean isSuccessful = false;
                String error = null;

                try {
                    isSuccessful = parsePaymentStatus(HttpUtils.sendPaymentStatusRequest(resourcePath, requestData.getServerMode(), requestData.getAuthorizationBearer(), Collections.singletonMap("entityId", requestData.getEntityId())));
                    if (isSuccessful) {
                        REQUEST_DATA_CACHE.remove(checkoutId);
                    }
                } catch (Exception var7) {
                    error = var7.getMessage();
                }

                listener.onResult(isSuccessful, error);
            }
        })).start();
    }

    @NonNull
    private static Map<String, String> prepareParameters(@NonNull String amount, @NonNull String currency, @Nullable String paymentType, @Nullable Map<String, String> extraParameters, boolean sendRegistrations) {
        Map<String, String> parameters = new HashMap();
        parameters.put("amount", amount);
        parameters.put("currency", currency);
        if (paymentType != null) {
            parameters.put("paymentType", paymentType);
        }

        if (extraParameters != null && extraParameters.size() > 0) {
            parameters.putAll(extraParameters);
        }

        if (!parameters.containsKey("entityId")) {
            parameters.put("entityId", "ff80808138516ef4013852936ec200f2");
        }

        if (sendRegistrations && "ff80808138516ef4013852936ec200f2".equals(parameters.get("entityId"))) {
            parameters.putAll(REGISTRATIONS);
        }

        return parameters;
    }

    @NonNull
    private static String getAuthorizationBearer(@Nullable String userId, @Nullable String password) {
        if (userId == null) {
            userId = "ff80808138516ef4013852936ec500f6";
        }

        if (password == null) {
            password = "8XJXcsjM";
        }

        return "Bearer " + Base64.encodeToString((userId + "|" + password).getBytes(StandardCharsets.UTF_8), 2);
    }

    @Nullable
    private static String parseCheckoutId(@NonNull String response) throws Exception {
        String checkoutId = (new JSONObject(response)).optString("id");
        return checkoutId.length() == 0 ? null : checkoutId;
    }

    private static boolean parsePaymentStatus(@NonNull String response) throws Exception {
        JSONObject responseJson = new JSONObject(response);
        if (!responseJson.has("result")) {
            return false;
        } else {
            String resultCode = responseJson.getJSONObject("result").optString("code");
            return resultCode.startsWith("000.") && !resultCode.startsWith("000.200");
        }
    }

    private static void putRequestDataToCache(@NonNull String checkoutId, @NonNull ServerMode serverMode, @NonNull String authorizationBearer, @NonNull String entityId) {
        REQUEST_DATA_CACHE.put(checkoutId, new RequestData(serverMode, authorizationBearer, entityId));
    }

    @Nullable
    private static String getRelatedCheckoutId(@NonNull String resourcePath) {
        Iterator var1 = REQUEST_DATA_CACHE.keySet().iterator();

        String checkoutId;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            checkoutId = (String) var1.next();
        } while (!resourcePath.contains(checkoutId));

        return checkoutId;
    }

    static {
        REGISTRATIONS.put("registrations[0].id", "8a82944a622a196201622a4b01ca3dc7");
        REGISTRATIONS.put("registrations[1].id", "8a8294495896823b01589b3a7efb4313");
        REGISTRATIONS.put("registrations[2].id", "8a82944a57c3d6610157d710b38a37ed");
        REGISTRATIONS.put("registrations[3].id", "8ac7a49f774dc63f01774e2425b723d3");
        REGISTRATIONS.put("registrations[4].id", "8ac7a49f774dc63f01775d84d7c31812");
        REQUEST_DATA_CACHE = new HashMap();
    }
}

