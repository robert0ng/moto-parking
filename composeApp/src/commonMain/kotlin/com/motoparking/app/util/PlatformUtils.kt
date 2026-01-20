package com.motoparking.app.util

/**
 * Platform-specific utilities for maps, navigation, and sharing.
 * Uses expect/actual pattern for KMP compatibility.
 */

/**
 * Opens the location in the device's default map application.
 * @param latitude Location latitude
 * @param longitude Location longitude
 * @param label Label to display for the location marker
 */
expect fun openInMaps(latitude: Double, longitude: Double, label: String)

/**
 * Opens navigation/directions to the specified location.
 * @param latitude Destination latitude
 * @param longitude Destination longitude
 * @param label Label for the destination
 */
expect fun navigateTo(latitude: Double, longitude: Double, label: String)

/**
 * Opens the system share sheet with the provided text content.
 * @param text The text to share
 * @param title Optional title for the share dialog
 */
expect fun shareText(text: String, title: String = "")
