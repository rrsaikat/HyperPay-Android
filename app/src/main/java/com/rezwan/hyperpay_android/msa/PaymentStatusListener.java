package com.rezwan.hyperpay_android.msa;

import androidx.annotation.Nullable;

@FunctionalInterface
public interface PaymentStatusListener {
    void onResult(boolean var1, @Nullable String var2);
}

