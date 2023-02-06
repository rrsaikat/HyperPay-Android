package com.rezwan.hyperpay_android

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.rezwan.hyperpay_android.base.BasePaymentActivity


/**
 * Represents an activity for making payments via {@link CheckoutActivity}.
 */
class CheckoutUIActivity : BasePaymentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_checkout_ui)

        val amount = config.AMOUNT + " " + config.CURRENCY

        findViewById<TextView>(R.id.amount_text_view).text = amount
        progressBar = findViewById<ProgressBar>(R.id.progress_bar_checkout_ui)

        findViewById<Button>(R.id.button_proceed_to_checkout).setOnClickListener {
            requestCheckoutId(
                config.AMOUNT,
                config.CURRENCY,
                config.PAYMENT_TYPE,
                config.SERVER_MODE,
                config.AUTHORIZATION,
                HashMap<String, String>().apply {
                    this["notificationUrl"] = config.NOTIFICATION_URL
                    this["entityId"] = config.MERCHANT_ID
                }
            )
        }
    }

    override fun onCheckoutIdReceived(checkoutId: String?) {
        super.onCheckoutIdReceived(checkoutId)
        if (checkoutId != null) {
            openCheckoutUI(checkoutId)
        }
    }

    private fun openCheckoutUI(checkoutId: String) {
        val checkoutSettings = createCheckoutSettings(
            checkoutId,
            getString(R.string.checkout_ui_callback_scheme),
            config.AMOUNT,
            config.CURRENCY,
            config.PAYMENT_BRANDS,
            config.MERCHANT_ID,
            config.PROVIDER_MODE
        )

        /* Start the checkout activity */
        checkoutLauncher.launch(checkoutSettings)
    }
}