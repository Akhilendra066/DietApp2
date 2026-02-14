import { Request } from 'express';

// ── Authenticated Request ──────────────────────────────
export interface AuthUser {
    id: string;
    email: string;
    role: string;
}

export interface AuthRequest extends Request {
    user?: AuthUser;
    accessToken?: string;
}

// ── API Response ───────────────────────────────────────
export interface ApiResponse<T = unknown> {
    success: boolean;
    data?: T;
    message?: string;
    error?: string;
    meta?: {
        page?: number;
        limit?: number;
        total?: number;
    };
}

// ── Profile ────────────────────────────────────────────
export interface Profile {
    id: string;
    user_id: string;
    full_name: string;
    display_name: string | null;
    avatar_url: string | null;
    date_of_birth: string | null;
    gender: 'male' | 'female' | 'other' | 'prefer_not_to_say' | null;
    height_cm: number | null;
    weight_kg: number | null;
    target_weight_kg: number | null;
    bmi: number | null;
    dietary_preference: string;
    activity_level: string;
    goal: string;
    target_calories: number | null;
    allergies: string[];
    medical_conditions: string[];
    preferred_cuisines: string[];
    language: string;
    timezone: string;
    onboarding_done: boolean;
    metadata: Record<string, unknown>;
}

// ── Meal Plan ──────────────────────────────────────────
export interface MealItem {
    meal_type: string;
    name: string;
    name_hindi?: string;
    calories: number;
    protein_g: number;
    carbs_g: number;
    fat_g: number;
    fiber_g: number;
    ingredients: string[];
    recipe_url?: string;
    prep_time_min?: number;
    is_vegetarian: boolean;
}

export interface MealPlanData {
    total_calories: number;
    total_protein_g: number;
    total_carbs_g: number;
    total_fat_g: number;
    total_fiber_g: number;
    meals: MealItem[];
    notes?: string;
}

// ── Chat ───────────────────────────────────────────────
export interface ChatMessage {
    role: 'user' | 'assistant' | 'system';
    content: string;
}

// ── Subscription ───────────────────────────────────────
export interface SubscriptionPlan {
    name: string;
    amount: number;
    currency: string;
    features: Record<string, unknown>;
    duration_days: number;
}

// ── App Error ──────────────────────────────────────────
export class AppError extends Error {
    public readonly statusCode: number;
    public readonly isOperational: boolean;
    public readonly details?: unknown;

    constructor(message: string, statusCode = 500, details?: unknown) {
        super(message);
        this.statusCode = statusCode;
        this.isOperational = true;
        this.details = details;
        Object.setPrototypeOf(this, AppError.prototype);
    }
}
