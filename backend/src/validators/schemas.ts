import { z } from 'zod';

export const signupSchema = z.object({
    email: z.string().email('Invalid email address').max(255),
    password: z.string().min(8, 'Password must be at least 8 characters').max(128),
    full_name: z.string().min(1, 'Full name is required').max(100).trim(),
    phone: z.string().regex(/^\+?[1-9]\d{6,14}$/, 'Invalid phone number').optional(),
});

export const loginSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(1, 'Password is required'),
});

export const updateProfileSchema = z.object({
    full_name: z.string().min(1).max(100).trim().optional(),
    display_name: z.string().max(50).trim().optional(),
    date_of_birth: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Use YYYY-MM-DD format').optional(),
    gender: z.enum(['male', 'female', 'other', 'prefer_not_to_say']).optional(),
    height_cm: z.number().min(30).max(300).optional(),
    weight_kg: z.number().min(10).max(500).optional(),
    target_weight_kg: z.number().min(10).max(500).optional(),
    dietary_preference: z.enum(['vegetarian', 'non_vegetarian', 'vegan', 'eggetarian', 'pescatarian', 'jain']).optional(),
    activity_level: z.enum(['sedentary', 'lightly_active', 'moderately_active', 'very_active', 'extra_active']).optional(),
    goal: z.enum(['lose_weight', 'gain_weight', 'maintain', 'muscle_gain', 'improve_health']).optional(),
    target_calories: z.number().int().min(500).max(10000).optional(),
    allergies: z.array(z.string().max(50)).max(20).optional(),
    medical_conditions: z.array(z.string().max(100)).max(20).optional(),
    preferred_cuisines: z.array(z.string().max(50)).max(10).optional(),
    language: z.string().min(2).max(10).optional(),
    timezone: z.string().max(50).optional(),
    onboarding_done: z.boolean().optional(),
});

export const generateMealPlanSchema = z.object({
    date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Use YYYY-MM-DD format').optional(),
    preferences: z.string().max(500).optional(),
});

export const swapMealSchema = z.object({
    meal_plan_id: z.string().uuid(),
    meal_index: z.number().int().min(0),
    reason: z.string().max(200).optional(),
});

export const weightLogSchema = z.object({
    log_date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Use YYYY-MM-DD format'),
    weight_kg: z.number().min(10).max(500),
    body_fat_pct: z.number().min(0).max(100).optional(),
    muscle_mass_kg: z.number().min(0).optional(),
    waist_cm: z.number().min(0).optional(),
    notes: z.string().max(500).optional(),
});

export const waterLogSchema = z.object({
    log_date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Use YYYY-MM-DD format'),
    amount_ml: z.number().int().min(1).max(5000),
});

export const mealCompleteSchema = z.object({
    log_date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Use YYYY-MM-DD format'),
    meal_type: z.enum(['breakfast', 'morning_snack', 'lunch', 'evening_snack', 'dinner', 'late_night']),
    meal_id: z.string().uuid().optional(),
    custom_name: z.string().max(200).optional(),
    quantity: z.number().min(0.1).max(100).default(1),
    calories: z.number().int().min(0),
    protein_g: z.number().min(0).default(0),
    carbs_g: z.number().min(0).default(0),
    fat_g: z.number().min(0).default(0),
    fiber_g: z.number().min(0).default(0),
    notes: z.string().max(500).optional(),
    mood: z.enum(['great', 'good', 'okay', 'bad', 'terrible']).optional(),
});

export const chatSchema = z.object({
    message: z.string().min(1, 'Message is required').max(2000),
    session_id: z.string().uuid().optional(),
});

export const subscribeSchema = z.object({
    plan_name: z.enum(['basic', 'premium', 'family']),
});

export const statsQuerySchema = z.object({
    from: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
    to: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
});
