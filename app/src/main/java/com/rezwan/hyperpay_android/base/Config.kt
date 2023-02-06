package com.rezwan.hyperpay_android.base

import com.oppwa.mobile.connect.provider.Connect
import com.oppwa.mobile.connect.provider.Connect.ProviderMode
import com.rezwan.hyperpay_android.msa.ServerMode


data class Config(
    var CONNECTION_TIMEOUT:Long = 5000,
    var BASE_URL:String = "http://52.59.56.185",
    var NOTIFICATION_URL:String = "$BASE_URL:80/notification",
    var MERCHANT_ID:String = "ff80808138516ef4013852936ec200f2",
    var SERVER_MODE: ServerMode = ServerMode.TEST,
    var PROVIDER_MODE:ProviderMode = Connect.ProviderMode.TEST,
    var LOG_TAG:String = "msdk.demo",
    var AUTHORIZATION:String= "",

    /* The configuration values to change across the app */
    /* The default amount and currency */
    var AMOUNT:String = "49.99",
    var CURRENCY:String = "EUR",
    var PAYMENT_TYPE:String = "PA",
    /* The payment brands for Ready-to-Use UI and Payment Button */
    var PAYMENT_BRANDS:Set<String> = linkedSetOf("VISA", "MASTER", "PAYPAL", "GOOGLEPAY"),
    /* The default payment brand for payment button */
    var PAYMENT_BUTTON_BRAND:String = "MASTER",

    /* The card info for SDK & Your Own UI */
    var CARD_BRAND:String = "VISA",
    var CARD_HOLDER_NAME:String = "JOHN DOE",
    var CARD_NUMBER:String = "4200000000000000",
    var CARD_EXPIRY_MONTH:String = "07",
    var CARD_EXPIRY_YEAR:String = "28",
    var CARD_CVV:String = "123"
)
