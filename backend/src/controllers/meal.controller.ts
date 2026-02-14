import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest, AppError, MealPlanData } from '../types';
import { sendSuccess, todayDateString } from '../utils/helpers';
import { aiService } from '../services/ai.service';
import { cacheService } from '../services/cache.service';

async function getUserProfile(userId: string) {
    const { data, error } = await supabaseAdmin
        .from('profiles')
        .select('*')
        .eq('user_id', userId)
        .eq('is_deleted', false)
        .single();

    if (error || !data) throw new AppError('Profile not found. Complete onboarding first.', 404);
    return data;
}

export async function generateMealPlan(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const { date, preferences } = req.body;
        const planDate = date || todayDateString();

        const profile = await getUserProfile(userId);
        const planData = await aiService.generateMealPlan(profile, planDate, preferences);

        // Save to DB
        const { data: saved, error } = await supabaseAdmin
            .from('meal_plans')
            .insert({
                user_id: userId,
                title: `Meal Plan - ${planDate}`,
                plan_date: planDate,
                is_ai_generated: true,
                ai_model: 'gemini-2.0-flash',
                plan_data: planData,
            })
            .select()
            .single();

        if (error) {
            logger.error('Failed to save meal plan', { error: error.message });
            throw new AppError('Failed to save meal plan', 500);
        }

        // Invalidate cached plans
        cacheService.delByPrefix(`meal_plans:${userId}`);

        logger.info(`Meal plan generated for user=${userId} date=${planDate}`);
        sendSuccess(res, saved, 'Meal plan generated', 201);
    } catch (err) {
        next(err);
    }
}

export async function getMealPlan(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const date = (req.query.date as string) || todayDateString();

        const cacheKey = `meal_plans:${userId}:${date}`;
        const cached = cacheService.get(cacheKey);
        if (cached) {
            sendSuccess(res, cached, 'Meal plan loaded (cached)');
            return;
        }

        const { data: plans, error } = await supabaseAdmin
            .from('meal_plans')
            .select('*')
            .eq('user_id', userId)
            .eq('plan_date', date)
            .eq('is_deleted', false)
            .order('created_at', { ascending: false })
            .limit(1);

        if (error) {
            throw new AppError('Failed to fetch meal plan', 500);
        }

        if (!plans || plans.length === 0) {
            sendSuccess(res, null, 'No meal plan found for this date');
            return;
        }

        cacheService.set(cacheKey, plans[0], 300);
        sendSuccess(res, plans[0], 'Meal plan loaded');
    } catch (err) {
        next(err);
    }
}

export async function swapMeal(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const { meal_plan_id, meal_index, reason } = req.body;

        // Fetch existing plan
        const { data: plan, error: fetchError } = await supabaseAdmin
            .from('meal_plans')
            .select('*')
            .eq('id', meal_plan_id)
            .eq('user_id', userId)
            .eq('is_deleted', false)
            .single();

        if (fetchError || !plan) {
            throw new AppError('Meal plan not found', 404);
        }

        const profile = await getUserProfile(userId);
        const currentPlanData = plan.plan_data as MealPlanData;
        const updatedPlanData = await aiService.swapMeal(profile, currentPlanData, meal_index, reason);

        // Update in DB
        const { data: updated, error: updateError } = await supabaseAdmin
            .from('meal_plans')
            .update({ plan_data: updatedPlanData })
            .eq('id', meal_plan_id)
            .select()
            .single();

        if (updateError) {
            throw new AppError('Failed to update meal plan', 500);
        }

        cacheService.delByPrefix(`meal_plans:${userId}`);

        logger.info(`Meal swapped for user=${userId} plan=${meal_plan_id} index=${meal_index}`);
        sendSuccess(res, updated, 'Meal swapped successfully');
    } catch (err) {
        next(err);
    }
}
