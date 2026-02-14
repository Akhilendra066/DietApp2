import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { env } from './env';
import { logger } from './logger';

// Admin client — bypasses RLS, used for server-side operations
export const supabaseAdmin: SupabaseClient = createClient(
    env.supabaseUrl,
    env.supabaseServiceRoleKey,
    {
        auth: {
            autoRefreshToken: false,
            persistSession: false,
        },
    }
);

// Creates a per-request client scoped to the user's JWT
export function createUserClient(accessToken: string): SupabaseClient {
    return createClient(env.supabaseUrl, env.supabaseAnonKey, {
        global: {
            headers: { Authorization: `Bearer ${accessToken}` },
        },
        auth: {
            autoRefreshToken: false,
            persistSession: false,
        },
    });
}

// Health check — verify connection
export async function checkDatabaseHealth(): Promise<boolean> {
    try {
        const { error } = await supabaseAdmin.from('users').select('id').limit(1);
        if (error) {
            logger.error('Database health check failed', { error: error.message });
            return false;
        }
        return true;
    } catch (err) {
        logger.error('Database health check exception', { error: err });
        return false;
    }
}
