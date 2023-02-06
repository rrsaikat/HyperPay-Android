package com.rezwan.hyperpay.builder

import com.rezwan.hyperpay.helper_model.PaymentResponse

interface OnPaymentResponseCallback {
    fun onPaymentResponseCallback(paymentResponse: PaymentResponse)
}