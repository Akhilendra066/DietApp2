import dotenv from 'dotenv';
dotenv.config();

function required(key: string): string {
    const value = process.env[key];
    if (!value) {
        throw new Error(`Missing required environment variable: ${key}`);
    }
    return value;
}

function optional(key: string, fallback: string): string {
    return process.env[key] || fallback;
}

export const env = {
    nodeEnv: optional('NODE_ENV', 'development'),
    port: parseInt(optional('PORT', '3000'), 10),
    isProduction: process.env.NODE_ENV === 'production',

    // Supabase
    supabaseUrl: required('SUPABASE_URL'),
    supabaseAnonKey: required('SUPABASE_ANON_KEY'),
    supabaseServiceRoleKey: required('SUPABASE_SERVICE_ROLE_KEY'),
    supabaseJwtSecret: required('SUPABASE_JWT_SECRET'),

    // JWT
    jwtSecret: required('JWT_SECRET'),
    jwtExpiresIn: optional('JWT_EXPIRES_IN', '7d'),
    jwtRefreshExpiresIn: optional('JWT_REFRESH_EXPIRES_IN', '30d'),

    // Gemini AI
    geminiApiKey: required('GEMINI_API_KEY'),
    geminiModel: optional('GEMINI_MODEL', 'gemini-2.0-flash'),

    // Razorpay
    razorpayKeyId: required('RAZORPAY_KEY_ID'),
    razorpayKeySecret: required('RAZORPAY_KEY_SECRET'),
    razorpayWebhookSecret: optional('RAZORPAY_WEBHOOK_SECRET', ''),

    // Rate Limiting
    rateLimitWindowMs: parseInt(optional('RATE_LIMIT_WINDOW_MS', '900000'), 10),
    rateLimitMaxRequests: parseInt(optional('RATE_LIMIT_MAX_REQUESTS', '100'), 10),

    // CORS
    corsOrigin: optional('CORS_ORIGIN', '*').split(',').map((s) => s.trim()),

    // Logging
    logLevel: optional('LOG_LEVEL', 'info'),
} as const;
