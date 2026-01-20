package com.motoparking.app.util

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSURL
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.NSCharacterSet
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.UIKit.UIApplication
import platform.UIKit.UIActivityViewController

@OptIn(BetaInteropApi::class)
private fun String.urlEncode(): String {
    val nsString = NSString.create(string = this)
    return nsString.stringByAddingPercentEncodingWithAllowedCharacters(
        NSCharacterSet.URLQueryAllowedCharacterSet
    ) ?: this
}

actual fun openInMaps(latitude: Double, longitude: Double, label: String) {
    val encodedLabel = label.urlEncode()

    // Use Apple Maps URL scheme
    val urlString = "http://maps.apple.com/?ll=$latitude,$longitude&q=$encodedLabel"
    val url = NSURL.URLWithString(urlString) ?: return

    UIApplication.sharedApplication.openURL(url)
}

actual fun navigateTo(latitude: Double, longitude: Double, label: String) {
    // Use Apple Maps with directions mode
    val urlString = "http://maps.apple.com/?daddr=$latitude,$longitude&dirflg=d"
    val url = NSURL.URLWithString(urlString) ?: return

    UIApplication.sharedApplication.openURL(url)
}

actual fun shareText(text: String, title: String) {
    val items = listOf(text)
    val activityViewController = UIActivityViewController(
        activityItems = items,
        applicationActivities = null
    )

    // Get the key window and root view controller
    val keyWindow = UIApplication.sharedApplication.keyWindow
    val rootViewController = keyWindow?.rootViewController

    rootViewController?.presentViewController(
        activityViewController,
        animated = true,
        completion = null
    )
}
