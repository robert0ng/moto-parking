package com.motoparking.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.lang.ref.WeakReference

/**
 * Android context provider for platform utilities.
 * Must be initialized in MainActivity.
 */
object AndroidContextProvider {
    private var contextRef: WeakReference<Context>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    fun getContext(): Context? = contextRef?.get()
}

actual fun openInMaps(latitude: Double, longitude: Double, label: String) {
    val context = AndroidContextProvider.getContext() ?: return
    val encodedLabel = Uri.encode(label)
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    // Try to open with Google Maps first, fallback to any map app
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Fallback to any map app
        intent.setPackage(null)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}

actual fun navigateTo(latitude: Double, longitude: Double, label: String) {
    val context = AndroidContextProvider.getContext() ?: return
    // Use Google Maps navigation URL scheme
    val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Fallback to generic geo intent with directions
        val fallbackUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (fallbackIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(fallbackIntent)
        }
    }
}

actual fun shareText(text: String, title: String) {
    val context = AndroidContextProvider.getContext() ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        if (title.isNotEmpty()) {
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooserIntent = Intent.createChooser(intent, title.ifEmpty { "分享" }).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooserIntent)
}
