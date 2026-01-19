package com.motoparking.shared.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    const val SUPABASE_URL = "https://zncrgcvrtvwfqyzjsoxf.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_qLtg7hC7YuDNJvBaVufmlQ_8YE2oIL1"
}

fun createSupabaseClient(): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = SupabaseConfig.SUPABASE_URL,
        supabaseKey = SupabaseConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
        // Auth module requires platform-specific session manager configuration
        // Will be added in Phase 2
    }
}
