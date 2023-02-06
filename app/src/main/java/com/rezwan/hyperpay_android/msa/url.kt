package com.rezwan.hyperpay_android.msa

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun Activity.openURL(link:String?){
    var url = link ?: ""
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://" + url
    }

    val uri = Uri.parse(url)

    try {
        /* Asynchronous transaction is processed in the onNewIntent() */
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER or
                    Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT
        }
        startActivity(intent)
    }catch (e: ActivityNotFoundException) {
        // Only browser apps are available, or a browser is the default app for this intent
        // This code executes in one of the following cases:
        // 1. Only browser apps can handle the intent.
        // 2. The user has set a browser app as the default app.
        // 3. The user hasn't set any app as the default for handling this URL.

        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, uri)
    }
}