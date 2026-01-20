package com.motoparking.shared.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    const val SUPABASE_URL = "https://zncrgcvrtvwfqyzjsoxf.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_qLtg7hC7YuDNJvBaVufmlQ_8YE2oIL1"

    // Google OAuth Web Client ID from Google Cloud Console
    // This is the OAuth 2.0 Client ID (Web application type) configured for Supabase
    // Setup: Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs
    const val GOOGLE_WEB_CLIENT_ID = "644927957739-6im0p5c61j1vv207gkc5sr4md317a70b.apps.googleusercontent.com"
}

fun createSupabaseClient(): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = SupabaseConfig.SUPABASE_URL,
        supabaseKey = SupabaseConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}
