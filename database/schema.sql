-- ============================================================================
-- AI DIET PLANNER — Production PostgreSQL Schema (Supabase Compatible)
-- Designed for 10,000+ concurrent users
-- ============================================================================

-- ========================== EXTENSIONS ======================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";   -- trigram index for fuzzy search

-- ========================== ENUMS ==========================================
CREATE TYPE dietary_preference AS ENUM (
    'vegetarian', 'non_vegetarian', 'vegan', 'eggetarian', 'pescatarian', 'jain'
);

CREATE TYPE gender_type AS ENUM ('male', 'female', 'other', 'prefer_not_to_say');

CREATE TYPE activity_level AS ENUM (
    'sedentary', 'lightly_active', 'moderately_active', 'very_active', 'extra_active'
);

CREATE TYPE meal_type AS ENUM ('breakfast', 'morning_snack', 'lunch', 'evening_snack', 'dinner', 'late_night');

CREATE TYPE goal_type AS ENUM ('lose_weight', 'gain_weight', 'maintain', 'muscle_gain', 'improve_health');

CREATE TYPE subscription_status AS ENUM ('trial', 'active', 'paused', 'cancelled', 'expired');

CREATE TYPE payment_status AS ENUM ('pending', 'completed', 'failed', 'refunded', 'disputed');

CREATE TYPE booking_status AS ENUM ('requested', 'confirmed', 'in_progress', 'completed', 'cancelled', 'no_show');

CREATE TYPE notification_type AS ENUM (
    'meal_reminder', 'water_reminder', 'weight_log', 'subscription',
    'chat', 'system', 'achievement', 'lab_report', 'booking'
);

CREATE TYPE log_mood AS ENUM ('great', 'good', 'okay', 'bad', 'terrible');

-- ========================== UTILITY FUNCTIONS ===============================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Soft delete: set deleted_at instead of removing
CREATE OR REPLACE FUNCTION soft_delete()
RETURNS TRIGGER AS $$
BEGIN
    NEW.deleted_at = NOW();
    NEW.is_deleted  = TRUE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 1. USERS (auth.users bridge — Supabase Auth)
-- ============================================================================
CREATE TABLE public.users (
    id            UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email         TEXT UNIQUE NOT NULL,
    phone         TEXT UNIQUE,
    role          TEXT NOT NULL DEFAULT 'user' CHECK (role IN ('user', 'admin', 'dietitian', 'moderator')),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email     ON public.users (email) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_phone     ON public.users (phone) WHERE phone IS NOT NULL AND is_deleted = FALSE;
CREATE INDEX idx_users_role      ON public.users (role) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_active    ON public.users (is_active) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 2. PROFILES
-- ============================================================================
CREATE TABLE public.profiles (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID NOT NULL UNIQUE REFERENCES public.users(id) ON DELETE CASCADE,
    full_name           TEXT NOT NULL DEFAULT '',
    display_name        TEXT,
    avatar_url          TEXT,
    date_of_birth       DATE,
    gender              gender_type,
    height_cm           NUMERIC(5,1) CHECK (height_cm > 0 AND height_cm < 300),
    weight_kg           NUMERIC(5,1) CHECK (weight_kg > 0 AND weight_kg < 500),
    target_weight_kg    NUMERIC(5,1) CHECK (target_weight_kg > 0 AND target_weight_kg < 500),
    bmi                 NUMERIC(4,1) GENERATED ALWAYS AS (
                            CASE WHEN height_cm > 0 AND weight_kg > 0
                                 THEN ROUND(weight_kg / ((height_cm / 100.0) ^ 2), 1)
                                 ELSE NULL END
                        ) STORED,
    dietary_preference  dietary_preference NOT NULL DEFAULT 'vegetarian',
    activity_level      activity_level NOT NULL DEFAULT 'sedentary',
    goal                goal_type NOT NULL DEFAULT 'maintain',
    target_calories     INT CHECK (target_calories > 0 AND target_calories < 10000),
    allergies           TEXT[] DEFAULT '{}',
    medical_conditions  TEXT[] DEFAULT '{}',
    preferred_cuisines  TEXT[] DEFAULT ARRAY['indian'],
    language            TEXT NOT NULL DEFAULT 'en',
    timezone            TEXT NOT NULL DEFAULT 'Asia/Kolkata',
    onboarding_done     BOOLEAN NOT NULL DEFAULT FALSE,
    metadata            JSONB DEFAULT '{}',
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_user_id     ON public.profiles (user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_profiles_diet_pref   ON public.profiles (dietary_preference) WHERE is_deleted = FALSE;
CREATE INDEX idx_profiles_goal        ON public.profiles (goal) WHERE is_deleted = FALSE;
CREATE INDEX idx_profiles_allergies   ON public.profiles USING GIN (allergies) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 3. MEAL PLANS (JSONB for AI-generated structured data)
-- ============================================================================
CREATE TABLE public.meal_plans (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title           TEXT NOT NULL,
    title_hindi     TEXT,
    description     TEXT,
    plan_date       DATE NOT NULL,
    is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    ai_model        TEXT,                   -- e.g. 'gemini-2.0-flash'
    ai_prompt       TEXT,                   -- prompt used for generation
    plan_data       JSONB NOT NULL DEFAULT '{}',
    /*
        plan_data schema:
        {
            "total_calories": 2000,
            "total_protein_g": 80,
            "total_carbs_g": 250,
            "total_fat_g": 65,
            "total_fiber_g": 30,
            "meals": [
                {
                    "meal_type": "breakfast",
                    "name": "Poha with vegetables",
                    "name_hindi": "सब्जी पोहा",
                    "calories": 350,
                    "protein_g": 8,
                    "carbs_g": 55,
                    "fat_g": 10,
                    "fiber_g": 4,
                    "ingredients": [...],
                    "recipe_url": "...",
                    "prep_time_min": 15,
                    "is_vegetarian": true
                }
            ],
            "notes": "..."
        }
    */
    total_calories  INT GENERATED ALWAYS AS ((plan_data->>'total_calories')::INT) STORED,
    is_favorite     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_meal_plans_user_date   ON public.meal_plans (user_id, plan_date DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_meal_plans_ai          ON public.meal_plans (user_id, is_ai_generated) WHERE is_deleted = FALSE;
CREATE INDEX idx_meal_plans_favorite    ON public.meal_plans (user_id) WHERE is_favorite = TRUE AND is_deleted = FALSE;
CREATE INDEX idx_meal_plans_plan_data   ON public.meal_plans USING GIN (plan_data jsonb_path_ops);
CREATE INDEX idx_meal_plans_calories    ON public.meal_plans (total_calories) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_meal_plans_updated_at
    BEFORE UPDATE ON public.meal_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 4. MEALS (individual food items / dishes — master catalog)
-- ============================================================================
CREATE TABLE public.meals (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            TEXT NOT NULL,
    name_hindi      TEXT,
    description     TEXT,
    category        TEXT NOT NULL DEFAULT 'general',
    -- e.g. 'dal', 'roti', 'rice', 'sabzi', 'curry', 'snack', 'beverage', 'dessert', 'salad'
    calories        INT NOT NULL CHECK (calories >= 0),
    protein_g       NUMERIC(6,1) NOT NULL DEFAULT 0 CHECK (protein_g >= 0),
    carbs_g         NUMERIC(6,1) NOT NULL DEFAULT 0 CHECK (carbs_g >= 0),
    fat_g           NUMERIC(6,1) NOT NULL DEFAULT 0 CHECK (fat_g >= 0),
    fiber_g         NUMERIC(6,1) NOT NULL DEFAULT 0 CHECK (fiber_g >= 0),
    serving_size    TEXT DEFAULT '1 serving',
    serving_weight_g NUMERIC(7,1),
    is_vegetarian   BOOLEAN NOT NULL DEFAULT TRUE,
    is_vegan        BOOLEAN NOT NULL DEFAULT FALSE,
    is_jain         BOOLEAN NOT NULL DEFAULT FALSE,
    is_gluten_free  BOOLEAN NOT NULL DEFAULT FALSE,
    allergens       TEXT[] DEFAULT '{}',
    image_url       TEXT,
    barcode         TEXT,
    source          TEXT DEFAULT 'manual', -- 'manual', 'api', 'ai', 'community'
    verified        BOOLEAN NOT NULL DEFAULT FALSE,
    metadata        JSONB DEFAULT '{}',
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_meals_name         ON public.meals USING GIN (name gin_trgm_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_meals_name_hindi   ON public.meals USING GIN (name_hindi gin_trgm_ops) WHERE name_hindi IS NOT NULL AND is_deleted = FALSE;
CREATE INDEX idx_meals_category     ON public.meals (category) WHERE is_deleted = FALSE;
CREATE INDEX idx_meals_vegetarian   ON public.meals (is_vegetarian) WHERE is_deleted = FALSE;
CREATE INDEX idx_meals_barcode      ON public.meals (barcode) WHERE barcode IS NOT NULL AND is_deleted = FALSE;
CREATE INDEX idx_meals_calories     ON public.meals (calories) WHERE is_deleted = FALSE;
CREATE INDEX idx_meals_verified     ON public.meals (verified) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_meals_updated_at
    BEFORE UPDATE ON public.meals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 5. GROCERY LISTS
-- ============================================================================
CREATE TABLE public.grocery_lists (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    meal_plan_id    UUID REFERENCES public.meal_plans(id) ON DELETE SET NULL,
    title           TEXT NOT NULL DEFAULT 'My Grocery List',
    items           JSONB NOT NULL DEFAULT '[]',
    /*
        items schema:
        [
            {
                "name": "Basmati Rice",
                "name_hindi": "बासमती चावल",
                "quantity": "1",
                "unit": "kg",
                "category": "grains",
                "is_checked": false,
                "estimated_price_inr": 120
            }
        ]
    */
    total_estimated_cost NUMERIC(8,2) DEFAULT 0,
    currency        TEXT NOT NULL DEFAULT 'INR',
    is_completed    BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_grocery_user       ON public.grocery_lists (user_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_grocery_meal_plan  ON public.grocery_lists (meal_plan_id) WHERE meal_plan_id IS NOT NULL AND is_deleted = FALSE;
CREATE INDEX idx_grocery_items      ON public.grocery_lists USING GIN (items jsonb_path_ops);

CREATE TRIGGER trg_grocery_lists_updated_at
    BEFORE UPDATE ON public.grocery_lists
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 6. DAILY LOGS
-- ============================================================================
CREATE TABLE public.daily_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    log_date        DATE NOT NULL,
    meal_type       meal_type NOT NULL,
    meal_id         UUID REFERENCES public.meals(id) ON DELETE SET NULL,
    custom_name     TEXT,               -- when meal not from catalog
    quantity        NUMERIC(5,2) NOT NULL DEFAULT 1 CHECK (quantity > 0),
    calories        INT NOT NULL CHECK (calories >= 0),
    protein_g       NUMERIC(6,1) NOT NULL DEFAULT 0,
    carbs_g         NUMERIC(6,1) NOT NULL DEFAULT 0,
    fat_g           NUMERIC(6,1) NOT NULL DEFAULT 0,
    fiber_g         NUMERIC(6,1) NOT NULL DEFAULT 0,
    notes           TEXT,
    image_url       TEXT,
    mood            log_mood,
    logged_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_synced       BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_daily_log_entry UNIQUE (user_id, log_date, meal_type, meal_id, created_at)
);

CREATE INDEX idx_daily_logs_user_date   ON public.daily_logs (user_id, log_date DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_daily_logs_meal_type   ON public.daily_logs (user_id, log_date, meal_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_daily_logs_date_range  ON public.daily_logs (log_date) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_daily_logs_updated_at
    BEFORE UPDATE ON public.daily_logs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 7. WEIGHT LOGS
-- ============================================================================
CREATE TABLE public.weight_logs (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    log_date    DATE NOT NULL,
    weight_kg   NUMERIC(5,1) NOT NULL CHECK (weight_kg > 0 AND weight_kg < 500),
    body_fat_pct NUMERIC(4,1) CHECK (body_fat_pct >= 0 AND body_fat_pct <= 100),
    muscle_mass_kg NUMERIC(5,1) CHECK (muscle_mass_kg >= 0),
    waist_cm    NUMERIC(5,1) CHECK (waist_cm > 0),
    notes       TEXT,
    image_url   TEXT, -- progress photo
    source      TEXT DEFAULT 'manual', -- 'manual', 'smart_scale', 'import'
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_weight_log_per_day UNIQUE (user_id, log_date)
);

CREATE INDEX idx_weight_logs_user_date ON public.weight_logs (user_id, log_date DESC) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_weight_logs_updated_at
    BEFORE UPDATE ON public.weight_logs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 8. WATER LOGS
-- ============================================================================
CREATE TABLE public.water_logs (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    log_date    DATE NOT NULL,
    amount_ml   INT NOT NULL CHECK (amount_ml > 0 AND amount_ml <= 5000),
    logged_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source      TEXT DEFAULT 'manual',
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_water_logs_user_date ON public.water_logs (user_id, log_date DESC) WHERE is_deleted = FALSE;

-- daily water summary view
CREATE OR REPLACE VIEW public.v_daily_water_summary AS
SELECT
    user_id,
    log_date,
    SUM(amount_ml) AS total_ml,
    COUNT(*)       AS entries
FROM public.water_logs
WHERE is_deleted = FALSE
GROUP BY user_id, log_date;

-- ============================================================================
-- 9. FAVORITES
-- ============================================================================
CREATE TABLE public.favorites (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    meal_id         UUID REFERENCES public.meals(id) ON DELETE CASCADE,
    meal_plan_id    UUID REFERENCES public.meal_plans(id) ON DELETE CASCADE,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_favorite_target CHECK (
        (meal_id IS NOT NULL AND meal_plan_id IS NULL) OR
        (meal_id IS NULL AND meal_plan_id IS NOT NULL)
    ),
    CONSTRAINT uq_fav_meal      UNIQUE (user_id, meal_id),
    CONSTRAINT uq_fav_meal_plan UNIQUE (user_id, meal_plan_id)
);

CREATE INDEX idx_favorites_user     ON public.favorites (user_id);
CREATE INDEX idx_favorites_meal     ON public.favorites (meal_id) WHERE meal_id IS NOT NULL;

-- ============================================================================
-- 10. CHAT HISTORY (AI diet assistant conversations)
-- ============================================================================
CREATE TABLE public.chat_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    session_id      UUID NOT NULL DEFAULT uuid_generate_v4(),
    role            TEXT NOT NULL CHECK (role IN ('user', 'assistant', 'system')),
    content         TEXT NOT NULL,
    content_type    TEXT NOT NULL DEFAULT 'text' CHECK (content_type IN ('text', 'image', 'meal_plan', 'action')),
    ai_model        TEXT,
    tokens_used     INT DEFAULT 0,
    metadata        JSONB DEFAULT '{}',
    /*
        metadata schema for meal_plan type:
        { "meal_plan_id": "...", "action": "generated" }
    */
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_user_session  ON public.chat_history (user_id, session_id, created_at) WHERE is_deleted = FALSE;
CREATE INDEX idx_chat_user_recent   ON public.chat_history (user_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_chat_session       ON public.chat_history (session_id) WHERE is_deleted = FALSE;

-- ============================================================================
-- 11. SUBSCRIPTIONS
-- ============================================================================
CREATE TABLE public.subscriptions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    plan_name       TEXT NOT NULL, -- 'free', 'basic', 'premium', 'family'
    status          subscription_status NOT NULL DEFAULT 'trial',
    trial_ends_at   TIMESTAMPTZ,
    starts_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ends_at         TIMESTAMPTZ,
    auto_renew      BOOLEAN NOT NULL DEFAULT TRUE,
    amount          NUMERIC(10,2) NOT NULL DEFAULT 0,
    currency        TEXT NOT NULL DEFAULT 'INR',
    payment_gateway TEXT, -- 'razorpay', 'stripe', 'google_play', 'apple_iap'
    gateway_sub_id  TEXT, -- external subscription ID
    features        JSONB DEFAULT '{}',
    /*
        features: {
            "ai_meal_plans_per_day": 5,
            "chat_messages_per_day": 50,
            "dietitian_bookings": true,
            "lab_report_analysis": true,
            "ad_free": true
        }
    */
    cancelled_at    TIMESTAMPTZ,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subs_user          ON public.subscriptions (user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_subs_status        ON public.subscriptions (status) WHERE is_deleted = FALSE;
CREATE INDEX idx_subs_active        ON public.subscriptions (user_id) WHERE status = 'active' AND is_deleted = FALSE;
CREATE INDEX idx_subs_expiring      ON public.subscriptions (ends_at) WHERE status = 'active' AND auto_renew = FALSE;

CREATE TRIGGER trg_subscriptions_updated_at
    BEFORE UPDATE ON public.subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 12. PAYMENTS
-- ============================================================================
CREATE TABLE public.payments (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    subscription_id     UUID REFERENCES public.subscriptions(id) ON DELETE SET NULL,
    booking_id          UUID,   -- FK added after bookings table
    amount              NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
    currency            TEXT NOT NULL DEFAULT 'INR',
    status              payment_status NOT NULL DEFAULT 'pending',
    payment_gateway     TEXT NOT NULL, -- 'razorpay', 'stripe', 'google_play', 'apple_iap'
    gateway_payment_id  TEXT,
    gateway_order_id    TEXT,
    gateway_signature   TEXT,
    receipt_url         TEXT,
    refund_amount       NUMERIC(10,2) DEFAULT 0,
    refunded_at         TIMESTAMPTZ,
    failure_reason      TEXT,
    metadata            JSONB DEFAULT '{}',
    paid_at             TIMESTAMPTZ,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_user          ON public.payments (user_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_payments_subscription  ON public.payments (subscription_id) WHERE subscription_id IS NOT NULL;
CREATE INDEX idx_payments_status        ON public.payments (status) WHERE is_deleted = FALSE;
CREATE INDEX idx_payments_gateway_id    ON public.payments (gateway_payment_id) WHERE gateway_payment_id IS NOT NULL;

CREATE TRIGGER trg_payments_updated_at
    BEFORE UPDATE ON public.payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 13. BOOKINGS (dietitian consultations)
-- ============================================================================
CREATE TABLE public.bookings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    dietitian_id    UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    status          booking_status NOT NULL DEFAULT 'requested',
    scheduled_at    TIMESTAMPTZ NOT NULL,
    duration_min    INT NOT NULL DEFAULT 30 CHECK (duration_min > 0 AND duration_min <= 180),
    meeting_url     TEXT,
    amount          NUMERIC(10,2) NOT NULL DEFAULT 0,
    currency        TEXT NOT NULL DEFAULT 'INR',
    notes           TEXT,
    user_notes      TEXT,              -- pre-consultation notes from user
    dietitian_notes TEXT,              -- post-consultation notes from dietitian
    rating          SMALLINT CHECK (rating >= 1 AND rating <= 5),
    review          TEXT,
    cancelled_by    UUID REFERENCES public.users(id),
    cancelled_at    TIMESTAMPTZ,
    cancel_reason   TEXT,
    completed_at    TIMESTAMPTZ,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_booking_users CHECK (user_id != dietitian_id)
);

-- Add FK from payments to bookings
ALTER TABLE public.payments
    ADD CONSTRAINT fk_payments_booking
    FOREIGN KEY (booking_id) REFERENCES public.bookings(id) ON DELETE SET NULL;

CREATE INDEX idx_bookings_user          ON public.bookings (user_id, scheduled_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_bookings_dietitian     ON public.bookings (dietitian_id, scheduled_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_bookings_status        ON public.bookings (status) WHERE is_deleted = FALSE;
CREATE INDEX idx_bookings_upcoming      ON public.bookings (scheduled_at) WHERE status IN ('confirmed', 'requested') AND is_deleted = FALSE;

CREATE TRIGGER trg_bookings_updated_at
    BEFORE UPDATE ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 14. LAB REPORTS
-- ============================================================================
CREATE TABLE public.lab_reports (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title           TEXT NOT NULL,
    report_date     DATE NOT NULL,
    lab_name        TEXT,
    file_url        TEXT,               -- uploaded PDF / image
    report_data     JSONB DEFAULT '{}',
    /*
        report_data schema:
        {
            "tests": [
                {
                    "name": "HbA1c",
                    "value": 5.6,
                    "unit": "%",
                    "normal_range": "4.0-5.6",
                    "status": "normal"
                },
                {
                    "name": "Vitamin D",
                    "value": 18,
                    "unit": "ng/mL",
                    "normal_range": "30-100",
                    "status": "low"
                }
            ],
            "ai_summary": "...",
            "ai_recommendations": ["..."]
        }
    */
    ai_analysis     TEXT,               -- AI-generated analysis
    ai_analyzed_at  TIMESTAMPTZ,
    verified_by     UUID REFERENCES public.users(id), -- dietitian who verified
    verified_at     TIMESTAMPTZ,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lab_reports_user       ON public.lab_reports (user_id, report_date DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_lab_reports_data       ON public.lab_reports USING GIN (report_data jsonb_path_ops);

CREATE TRIGGER trg_lab_reports_updated_at
    BEFORE UPDATE ON public.lab_reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 15. NOTIFICATIONS
-- ============================================================================
CREATE TABLE public.notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    type            notification_type NOT NULL,
    title           TEXT NOT NULL,
    body            TEXT NOT NULL,
    data            JSONB DEFAULT '{}',
    /*
        data: { "screen": "meal_plan", "id": "..." }
    */
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    is_pushed       BOOLEAN NOT NULL DEFAULT FALSE,
    pushed_at       TIMESTAMPTZ,
    scheduled_for   TIMESTAMPTZ,        -- future-scheduled notification
    expires_at      TIMESTAMPTZ,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifs_user_unread     ON public.notifications (user_id, created_at DESC) WHERE is_read = FALSE AND is_deleted = FALSE;
CREATE INDEX idx_notifs_user_all        ON public.notifications (user_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_notifs_scheduled       ON public.notifications (scheduled_for) WHERE is_pushed = FALSE AND scheduled_for IS NOT NULL AND is_deleted = FALSE;
CREATE INDEX idx_notifs_type            ON public.notifications (user_id, type) WHERE is_deleted = FALSE;

-- ============================================================================
-- MATERIALIZED VIEW — Daily Nutrition Aggregation (performance)
-- ============================================================================
CREATE MATERIALIZED VIEW public.mv_daily_nutrition AS
SELECT
    dl.user_id,
    dl.log_date,
    SUM(dl.calories)    AS total_calories,
    SUM(dl.protein_g)   AS total_protein_g,
    SUM(dl.carbs_g)     AS total_carbs_g,
    SUM(dl.fat_g)       AS total_fat_g,
    SUM(dl.fiber_g)     AS total_fiber_g,
    COUNT(*)            AS total_entries,
    COUNT(DISTINCT dl.meal_type) AS meals_logged,
    COALESCE(w.total_ml, 0)     AS water_ml
FROM public.daily_logs dl
LEFT JOIN public.v_daily_water_summary w
    ON dl.user_id = w.user_id AND dl.log_date = w.log_date
WHERE dl.is_deleted = FALSE
GROUP BY dl.user_id, dl.log_date, w.total_ml;

CREATE UNIQUE INDEX idx_mv_daily_nutrition ON public.mv_daily_nutrition (user_id, log_date);

-- Refresh function (call via cron or Edge Function)
CREATE OR REPLACE FUNCTION refresh_daily_nutrition()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY public.mv_daily_nutrition;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- ROW LEVEL SECURITY POLICIES
-- ============================================================================

-- Helper: get current user's ID
CREATE OR REPLACE FUNCTION public.uid_safe()
RETURNS UUID AS $$
    SELECT COALESCE(auth.uid(), '00000000-0000-0000-0000-000000000000'::UUID);
$$ LANGUAGE sql STABLE;

-- Helper: check if current user is admin
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.users
        WHERE id = auth.uid() AND role = 'admin' AND is_deleted = FALSE
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- ---------- USERS ----------
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own record"
    ON public.users FOR SELECT
    USING (id = auth.uid());

CREATE POLICY "Admins can view all users"
    ON public.users FOR SELECT
    USING (public.is_admin());

CREATE POLICY "Users can update own record"
    ON public.users FOR UPDATE
    USING (id = auth.uid());

-- ---------- PROFILES ----------
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (user_id = auth.uid());

CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (user_id = auth.uid());

CREATE POLICY "Dietitians can view client profiles"
    ON public.profiles FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.bookings b
            WHERE b.dietitian_id = auth.uid()
              AND b.user_id = profiles.user_id
              AND b.status IN ('confirmed', 'in_progress', 'completed')
              AND b.is_deleted = FALSE
        )
    );

-- ---------- MEAL PLANS ----------
ALTER TABLE public.meal_plans ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own meal plans"
    ON public.meal_plans FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---------- MEALS (public catalog) ----------
ALTER TABLE public.meals ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read meals"
    ON public.meals FOR SELECT
    USING (is_deleted = FALSE);

CREATE POLICY "Admins can manage meals"
    ON public.meals FOR ALL
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- ---------- GROCERY LISTS ----------
ALTER TABLE public.grocery_lists ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own grocery lists"
    ON public.grocery_lists FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---------- DAILY LOGS ----------
ALTER TABLE public.daily_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own daily logs"
    ON public.daily_logs FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---------- WEIGHT LOGS ----------
ALTER TABLE public.weight_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own weight logs"
    ON public.weight_logs FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---------- WATER LOGS ----------
ALTER TABLE public.water_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own water logs"
    ON public.water_logs FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---------- FAVORITES ----------
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own favorites"
    ON public.favorites FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---------- CHAT HISTORY ----------
ALTER TABLE public.chat_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own chat history"
    ON public.chat_history FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ---------- SUBSCRIPTIONS ----------
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own subscriptions"
    ON public.subscriptions FOR SELECT
    USING (user_id = auth.uid());

CREATE POLICY "Admins can manage subscriptions"
    ON public.subscriptions FOR ALL
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- ---------- PAYMENTS ----------
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own payments"
    ON public.payments FOR SELECT
    USING (user_id = auth.uid());

CREATE POLICY "Admins can manage payments"
    ON public.payments FOR ALL
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- ---------- BOOKINGS ----------
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own bookings"
    ON public.bookings FOR SELECT
    USING (user_id = auth.uid() OR dietitian_id = auth.uid());

CREATE POLICY "Users can create bookings"
    ON public.bookings FOR INSERT
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Participants can update bookings"
    ON public.bookings FOR UPDATE
    USING (user_id = auth.uid() OR dietitian_id = auth.uid());

-- ---------- LAB REPORTS ----------
ALTER TABLE public.lab_reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can CRUD own lab reports"
    ON public.lab_reports FOR ALL
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Dietitians can view client lab reports"
    ON public.lab_reports FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.bookings b
            WHERE b.dietitian_id = auth.uid()
              AND b.user_id = lab_reports.user_id
              AND b.status IN ('confirmed', 'in_progress', 'completed')
              AND b.is_deleted = FALSE
        )
    );

-- ---------- NOTIFICATIONS ----------
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own notifications"
    ON public.notifications FOR SELECT
    USING (user_id = auth.uid());

CREATE POLICY "Users can update own notifications"
    ON public.notifications FOR UPDATE
    USING (user_id = auth.uid());

-- ============================================================================
-- TRIGGERS — Auto-create profile on user insert
-- ============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (user_id, full_name)
    VALUES (NEW.id, COALESCE(NEW.email, ''));

    INSERT INTO public.subscriptions (user_id, plan_name, status, trial_ends_at)
    VALUES (NEW.id, 'free', 'trial', NOW() + INTERVAL '7 days');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trg_on_user_created
    AFTER INSERT ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================================
-- TRIGGERS — Mark notification as read
-- ============================================================================
CREATE OR REPLACE FUNCTION public.mark_notification_read()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_read = TRUE AND OLD.is_read = FALSE THEN
        NEW.read_at = NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notification_read
    BEFORE UPDATE ON public.notifications
    FOR EACH ROW EXECUTE FUNCTION public.mark_notification_read();

-- ============================================================================
-- TRIGGERS — Update profile weight from weight_logs
-- ============================================================================
CREATE OR REPLACE FUNCTION public.sync_profile_weight()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE public.profiles
    SET weight_kg = NEW.weight_kg
    WHERE user_id = NEW.user_id AND is_deleted = FALSE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trg_sync_weight_to_profile
    AFTER INSERT ON public.weight_logs
    FOR EACH ROW EXECUTE FUNCTION public.sync_profile_weight();

-- ============================================================================
-- SEED DATA — Meal Categories & Sample Indian Meals
-- ============================================================================

-- Indian Breakfast Items
INSERT INTO public.meals (name, name_hindi, category, calories, protein_g, carbs_g, fat_g, fiber_g, is_vegetarian, serving_size, verified) VALUES
('Poha (Flattened Rice)',          'पोहा',          'breakfast', 250, 5.0,  45.0, 6.0,  2.0, TRUE,  '1 plate (200g)',  TRUE),
('Idli with Sambhar',              'इडली सांभर',     'breakfast', 200, 6.0,  38.0, 2.5,  3.0, TRUE,  '3 pieces',        TRUE),
('Masala Dosa',                    'मसाला डोसा',     'breakfast', 370, 7.0,  50.0, 15.0, 3.0, TRUE,  '1 dosa',          TRUE),
('Upma',                           'उपमा',          'breakfast', 220, 5.0,  35.0, 7.0,  2.5, TRUE,  '1 bowl (200g)',   TRUE),
('Aloo Paratha',                   'आलू पराठा',      'breakfast', 300, 6.0,  40.0, 13.0, 3.0, TRUE,  '1 paratha',       TRUE),
('Moong Dal Chilla',               'मूंग दाल चीला',   'breakfast', 180, 10.0, 22.0, 5.0,  4.0, TRUE,  '2 chillas',       TRUE),
('Egg Bhurji with Toast',          'अंडा भुर्जी',     'breakfast', 320, 18.0, 25.0, 16.0, 2.0, FALSE, '2 eggs + 2 toast', TRUE),
('Oats Porridge with Fruits',      'ओट्स',           'breakfast', 200, 7.0,  35.0, 4.0,  5.0, TRUE,  '1 bowl',          TRUE);

-- Indian Lunch / Dinner Items
INSERT INTO public.meals (name, name_hindi, category, calories, protein_g, carbs_g, fat_g, fiber_g, is_vegetarian, serving_size, verified) VALUES
('Dal Tadka',                      'दाल तड़का',       'dal',       180, 12.0, 25.0, 4.0,  6.0, TRUE,  '1 bowl (200ml)',  TRUE),
('Rajma Chawal',                   'राजमा चावल',     'dal',       420, 15.0, 70.0, 7.0,  8.0, TRUE,  '1 plate',         TRUE),
('Palak Paneer',                   'पालक पनीर',      'sabzi',     260, 14.0, 10.0, 18.0, 4.0, TRUE,  '1 bowl (200g)',   TRUE),
('Chicken Curry',                  'चिकन करी',       'curry',     350, 30.0, 12.0, 20.0, 2.0, FALSE, '1 bowl (200g)',   TRUE),
('Chole',                          'छोले',           'dal',       280, 12.0, 40.0, 8.0,  10.0,TRUE,  '1 bowl (200g)',   TRUE),
('Mixed Vegetable Sabzi',          'मिक्स सब्जी',    'sabzi',     120, 4.0,  15.0, 5.0,  5.0, TRUE,  '1 bowl (200g)',   TRUE),
('Tandoori Roti',                  'तंदूरी रोटी',     'roti',      120, 4.0,  22.0, 1.5,  2.0, TRUE,  '1 roti',          TRUE),
('Steamed Basmati Rice',           'बासमती चावल',    'rice',      200, 4.0,  45.0, 0.5,  1.0, TRUE,  '1 cup cooked',   TRUE),
('Jeera Rice',                     'जीरा चावल',      'rice',      230, 4.5,  44.0, 4.0,  1.5, TRUE,  '1 cup cooked',   TRUE),
('Raita',                          'रायता',          'side',       80, 3.0,   6.0, 4.0,  0.5, TRUE,  '1 bowl (150ml)',  TRUE),
('Green Salad',                    'हरा सलाद',       'salad',      40, 1.5,   7.0, 0.5,  3.0, TRUE,  '1 bowl',          TRUE),
('Fish Curry',                     'मछली करी',       'curry',     280, 25.0,  8.0, 16.0, 1.0, FALSE, '1 bowl (200g)',   TRUE);

-- Indian Snacks
INSERT INTO public.meals (name, name_hindi, category, calories, protein_g, carbs_g, fat_g, fiber_g, is_vegetarian, serving_size, verified) VALUES
('Sprouts Chaat',                  'अंकुरित चाट',    'snack',     150, 8.0,  22.0, 3.0,  5.0, TRUE,  '1 bowl',          TRUE),
('Roasted Makhana',                'भुनी मखाना',     'snack',     100, 3.0,  18.0, 1.0,  1.5, TRUE,  '1 cup',           TRUE),
('Fruit Bowl',                     'फ्रूट बाउल',     'snack',     120, 1.5,  30.0, 0.5,  4.0, TRUE,  '1 bowl',          TRUE),
('Dhokla',                         'ढोकला',          'snack',     160, 5.0,  25.0, 4.0,  2.0, TRUE,  '3 pieces',        TRUE),
('Paneer Tikka',                   'पनीर टिक्का',    'snack',     250, 16.0, 8.0,  18.0, 1.0, TRUE,  '6 pieces',        TRUE);

-- Beverages
INSERT INTO public.meals (name, name_hindi, category, calories, protein_g, carbs_g, fat_g, fiber_g, is_vegetarian, serving_size, verified) VALUES
('Masala Chai',                    'मसाला चाय',      'beverage',   80, 2.0,  12.0, 2.5,  0.0, TRUE,  '1 cup (200ml)',   TRUE),
('Buttermilk (Chaas)',             'छाछ',            'beverage',   40, 2.0,   5.0, 1.0,  0.0, TRUE,  '1 glass (250ml)', TRUE),
('Lassi (Sweet)',                  'मीठी लस्सी',     'beverage',  160, 5.0,  25.0, 4.0,  0.0, TRUE,  '1 glass (300ml)', TRUE),
('Coconut Water',                  'नारियल पानी',    'beverage',   45, 0.5,  10.0, 0.0,  2.5, TRUE,  '1 glass (250ml)', TRUE),
('Green Tea',                      'ग्रीन टी',       'beverage',    2, 0.0,   0.0, 0.0,  0.0, TRUE,  '1 cup (200ml)',   TRUE);

-- ============================================================================
-- SEED DATA — Subscription Plans
-- ============================================================================
-- Note: Real subscriptions are auto-created via the handle_new_user trigger
-- when a new user signs up. No manual seed data needed here.

-- ============================================================================
-- PERFORMANCE: ANALYZE after bulk inserts
-- ============================================================================
ANALYZE public.meals;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
