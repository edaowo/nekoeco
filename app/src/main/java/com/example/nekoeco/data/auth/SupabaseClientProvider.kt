package com.example.nekoeco.data.auth

import com.example.nekoeco.BuildConfig
import com.example.nekoeco.data.auth.supabaseSecret.SUPABASE_ANON_KEY
import com.example.nekoeco.data.auth.supabaseSecret.SUPABASE_URL
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            // ================================================
            // Install Postgrest only (no Auth module).
            // ================================================
            install(Postgrest)
        }
    }
}