package com.rezwan.hyperpay_android.base

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.oppwa.mobile.connect.checkout.meta.CheckoutActivityResult
import com.oppwa.mobile.connect.checkout.meta.CheckoutActivityResultContract
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings
import com.oppwa.mobile.connect.checkout.meta.CheckoutSkipCVVMode
import com.oppwa.mobile.connect.exception.PaymentError
import com.oppwa.mobile.connect.provider.Connect
import com.oppwa.mobile.connect.provider.Transaction
import com.oppwa.mobile.connect.provider.TransactionType
import com.oppwa.mobile.connect.utils.googlepay.CardPaymentMethodJsonBuilder
import com.oppwa.mobile.connect.utils.googlepay.PaymentDataRequestJsonBuilder
import com.oppwa.mobile.connect.utils.googlepay.TransactionInfoJsonBuilder
import com.rezwan.hyperpay_android.R
import com.rezwan.hyperpay_android.msa.Msa
import com.rezwan.hyperpay_android.msa.ServerMode
import com.rezwan.hyperpay_android.msa.openURL
import com.rezwan.hyperpay_android.receiver.CheckoutBroadcastReceiver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONArray

/**
 * Represents a base activity for making the payments with mobile sdk.
 * This activity handles payment callbacks.
 */
open class BasePaymentActivity : BaseActivity() {
    val config = Config().apply {
//        BASE_URL = "https://test.oppwa.com"
//        AMOUNT = "10.99"
//        CURRENCY = "SAR"
//        PAYMENT_TYPE = "DB"
//        AUTHORIZATION = "Bearer OG44Mjk0MTc0ZDA1OTVitjAxNGQwNWQ4MjloNzAxZDF8OVRuSlBjMm45aP=="
//        MERCHANT_ID = "ff80808138516ef4013852936ec200f2" //Entity ID of VISA/MASTER
//        PAYMENT_BRANDS = linkedSetOf("VISA", "MASTER", "PAYPAL", "GOOGLEPAY")
//        SERVER_MODE = ServerMode.TEST
//        PROVIDER_MODE = Connect.ProviderMode.TEST
    }

    companion object {
        const val STATE_RESOURCE_PATH = "STATE_RESOURCE_PATH"
    }

    protected val checkoutLauncher = registerForActivityResult(
        CheckoutActivityResultContract()
    ) { result: CheckoutActivityResult ->
        this.handleCheckoutActivityResult(result)
    }

    protected var resourcePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            resourcePath = savedInstanceState.getString(STATE_RESOURCE_PATH)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        /* Check of the intent contains the callback scheme */
        if (resourcePath != null && hasCallBackScheme(intent)) {
            requestPaymentStatus(resourcePath!!)
        }
    }

    private fun handleCheckoutActivityResult(result: CheckoutActivityResult) {
        hideProgressBar()

        if (result.isCanceled) {
            return
        }

        if (result.isErrored) {
            val error: PaymentError? = result.paymentError
            error?.let { showAlertDialog(it.errorMessage) }

            return
        }

        /* Transaction completed */
        val transaction: Transaction? = result.transaction

        resourcePath = result.resourcePath

        /* Check the transaction type */
        if (transaction != null) {
            if (transaction.transactionType == TransactionType.SYNC) {
                /* Check the status of synchronous transaction */
                requestPaymentStatus(resourcePath!!)
            } else {
                openURL(transaction.redirectUrl)
            }
        }
    }

    open fun onCheckoutIdReceived(checkoutId: String?) {
        if (checkoutId == null) {
            hideProgressBar()
            showAlertDialog(R.string.error_message)
        }
    }

    protected fun requestCheckoutId(
        amount: String,
        currency: String,
        paymentType: String,
        serverMode: ServerMode,
        authorization: String,
        extraParameters: Map<String, String>
    ) {
        showProgressBar()

        Msa.requestCheckoutId(
            amount,
            currency,
            paymentType,
            serverMode,
            authorization,
            extraParameters
        ) { checkoutId, _ -> runOnUiThread { onCheckoutIdReceived(checkoutId) } }
    }

    protected fun requestPaymentStatus(resourcePath: String) {
        showProgressBar()

        Msa.requestPaymentStatus(
            resourcePath
        ) { isSuccessful, error ->
            runOnUiThread {
                onPaymentStatusReceived(isSuccessful, error)
            }
        }
    }

    protected fun createCheckoutSettings(
        checkoutId: String,
        callbackScheme: String,
        amount: String,
        currency: String,
        brands: Set<String>,
        merchantId: String,
        mode: Connect.ProviderMode
    ): CheckoutSettings {
        return CheckoutSettings(
            checkoutId, brands, mode
        )
            .setSkipCVVMode(CheckoutSkipCVVMode.FOR_STORED_CARDS)
            .setShopperResultUrl("$callbackScheme://callback")
            .setGooglePayPaymentDataRequestJson(
                getGooglePayPaymentDataRequestJson(
                    merchantId,
                    amount,
                    currency
                )
            )
            /* Set componentName if you want to receive callbacks from the checkout */
            .setComponentName(
                ComponentName(
                    packageName,
                    CheckoutBroadcastReceiver::class.java.name
                )
            )
    }

    private fun hasCallBackScheme(intent: Intent): Boolean {
        return intent.scheme == getString(R.string.checkout_ui_callback_scheme) ||
                intent.scheme == getString(R.string.payment_button_callback_scheme) ||
                intent.scheme == getString(R.string.custom_ui_callback_scheme)
    }

    private fun onPaymentStatusReceived(isSuccessful: Boolean, error: String?) {
        hideProgressBar()
        val message = if (isSuccessful) {
            getString(R.string.message_successful_payment)
        } else {
            getString(R.string.message_unsuccessful_payment) + "\n" + error ?: ""
        }

        showAlertDialog(message)
    }

    private fun getGooglePayPaymentDataRequestJson(
        merchantId: String, amount: String,
        currency: String
    ): String {
        val allowedPaymentMethods = JSONArray()
            .put(
                CardPaymentMethodJsonBuilder()
                    .setAllowedAuthMethods(
                        JSONArray()
                            .put("PAN_ONLY")
                            .put("CRYPTOGRAM_3DS")
                    )
                    .setAllowedCardNetworks(
                        JSONArray()
                            .put("VISA")
                            .put("MASTERCARD")
                            .put("AMEX")
                            .put("DISCOVER")
                            .put("JCB")
                    )
                    .setGatewayMerchantId(merchantId)
                    .toJson()
            )

        val transactionInfo = TransactionInfoJsonBuilder()
            .setCurrencyCode(currency)
            .setTotalPriceStatus("FINAL")
            .setTotalPrice(amount)
            .toJson()

        val paymentDataRequest = PaymentDataRequestJsonBuilder()
            .setAllowedPaymentMethods(allowedPaymentMethods)
            .setTransactionInfo(transactionInfo)
            .toJson()

        return paymentDataRequest.toString()
    }
}