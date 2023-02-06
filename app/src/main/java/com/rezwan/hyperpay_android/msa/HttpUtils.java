package com.rezwan.hyperpay_android.msa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

class HttpUtils {
    static final String METHOD_GET = "GET";
    static final String METHOD_POST = "POST";
    private static final String TEST_URL = "https://eu-test.oppwa.com";
    private static final String LIVE_URL = "https://eu-prod.oppwa.com";

    HttpUtils() {
    }

    @NonNull
    static String sendCheckoutIdRequest(@NonNull ServerMode serverMode, @NonNull String authorizationBearer, @NonNull Map<String, String> parameters) throws Exception {
        return sendRequest("POST", "/v1/checkouts", serverMode, authorizationBearer, parameters);
    }

    @NonNull
    static String sendPaymentStatusRequest(@NonNull String resourcePath, @NonNull ServerMode serverMode, @NonNull String authorizationBearer, @NonNull Map<String, String> parameters) throws Exception {
        return sendRequest("GET", resourcePath, serverMode, authorizationBearer, parameters);
    }

    @NonNull
    static String sendRequest(@NonNull String method, @NonNull String path, @NonNull ServerMode serverMode, @Nullable String authorizationBearer, @NonNull Map<String, String> parameters) throws Exception {
        URL url;
        if ("GET".equals(method)) {
            url = new URL(getUrl(serverMode) + path + "?" + getParametersString(parameters));
        } else {
            url = new URL(getUrl(serverMode) + path);
        }

        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestMethod(method);
        if (authorizationBearer != null) {
            connection.setRequestProperty("Authorization", authorizationBearer);
        }

        if ("POST".equals(method)) {
            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

            try {
                outputStream.writeBytes(getParametersString(parameters));
                outputStream.flush();
            } catch (Throwable var11) {
                try {
                    outputStream.close();
                } catch (Throwable var10) {
                    var11.addSuppressed(var10);
                }

                throw var11;
            }

            outputStream.close();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 400) {
            throw new Exception(convertInputStreamToString(connection.getErrorStream()));
        } else {
            return convertInputStreamToString(connection.getInputStream());
        }
    }

    @NonNull
    private static String getUrl(@NonNull ServerMode serverMode) {
        return serverMode == ServerMode.LIVE ? "https://eu-prod.oppwa.com" : "https://eu-test.oppwa.com";
    }

    @NonNull
    private static String getParametersString(@NonNull Map<String, String> parameters) {
        StringBuilder parametersString = new StringBuilder();
        Iterator var2 = parameters.keySet().iterator();

        while(var2.hasNext()) {
            String key = (String)var2.next();
            parametersString.append(key);
            parametersString.append('=');
            parametersString.append((String)parameters.get(key));
            parametersString.append('&');
        }

        parametersString.delete(parametersString.length() - 1, parametersString.length());
        return parametersString.toString();
    }

    @NonNull
    private static String convertInputStreamToString(@NonNull InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String var4;
        try {
            StringBuilder result = new StringBuilder();

            while(true) {
                String line;
                if ((line = reader.readLine()) == null) {
                    var4 = result.toString();
                    break;
                }

                result.append(line);
            }
        } catch (Throwable var6) {
            try {
                reader.close();
            } catch (Throwable var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }

        reader.close();
        return var4;
    }
}

