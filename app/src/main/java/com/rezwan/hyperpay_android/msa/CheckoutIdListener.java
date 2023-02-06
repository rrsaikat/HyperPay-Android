package com.rezwan.hyperpay_android.msa;

import androidx.annotation.Nullable;

@FunctionalInterface
public interface CheckoutIdListener {
    void onResult(@Nullable String var1, @Nullable String var2);
}
