package com.example.naturepin;

import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.SupabaseClientBuilder;
import io.github.jan.supabase.auth.Auth;
import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.storage.Storage;
import kotlin.Unit;

public class SupabaseManager {
    private static final String SUPABASE_URL = "https://pfmyrgsvwnvtqzjgicdm.supabase.co";
    private static final String SUPABASE_KEY = "sb_publishable_yBysASZY8s3hvlFE-WF-QA_dMiQqZ-z";
    private static SupabaseClient client;

    public static SupabaseClient getClient() {
        if (client == null) {
            SupabaseClientBuilder builder = new SupabaseClientBuilder(SUPABASE_URL, SUPABASE_KEY);
            builder.install(Auth.Companion, config -> Unit.INSTANCE);
            builder.install(Postgrest.Companion, config -> Unit.INSTANCE);
            builder.install(Storage.Companion, config -> Unit.INSTANCE);
            client = builder.build();
        }
        return client;
    }
}
