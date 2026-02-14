import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest, AppError } from '../types';
import { sendSuccess } from '../utils/helpers';

export async function logWeight(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const { log_date, weight_kg, body_fat_pct, muscle_mass_kg, waist_cm, notes } = req.body;

        // Upsert — one weight log per user per day
        const { data, error } = await supabaseAdmin
            .from('weight_logs')
            .upsert(
                {
                    user_id: userId,
                    log_date,
                    weight_kg,
                    body_fat_pct: body_fat_pct || null,
                    muscle_mass_kg: muscle_mass_kg || null,
                    waist_cm: waist_cm || null,
                    notes: notes || null,
                },
                { onConflict: 'user_id,log_date' }
            )
            .select()
            .single();

        if (error) {
            logger.error('Weight log failed', { error: error.message });
            throw new AppError('Failed to log weight', 500);
        }

        // The DB trigger sync_profile_weight auto-updates the profile

        logger.info(`Weight logged: user=${userId} date=${log_date} weight=${weight_kg}kg`);
        sendSuccess(res, data, 'Weight logged', 201);
    } catch (err) {
        next(err);
    }
}

export async function logWater(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const { log_date, amount_ml } = req.body;

        const { data, error } = await supabaseAdmin
            .from('water_logs')
            .insert({
                user_id: userId,
                log_date,
                amount_ml,
            })
            .select()
            .single();

        if (error) {
            logger.error('Water log failed', { error: error.message });
            throw new AppError('Failed to log water', 500);
        }

        logger.info(`Water logged: user=${userId} date=${log_date} amount=${amount_ml}ml`);
        sendSuccess(res, data, 'Water intake logged', 201);
    } catch (err) {
        next(err);
    }
}

export async function logMealComplete(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const {
            log_date, meal_type, meal_id, custom_name,
            quantity, calories, protein_g, carbs_g, fat_g, fiber_g,
            notes, mood,
        } = req.body;

        const { data, error } = await supabaseAdmin
            .from('daily_logs')
            .insert({
                user_id: userId,
                log_date,
                meal_type,
                meal_id: meal_id || null,
                custom_name: custom_name || null,
                quantity,
                calories,
                protein_g,
                carbs_g,
                fat_g,
                fiber_g,
                notes: notes || null,
                mood: mood || null,
            })
            .select()
            .single();

        if (error) {
            logger.error('Meal log failed', { error: error.message });
            throw new AppError('Failed to log meal', 500);
        }

        logger.info(`Meal logged: user=${userId} date=${log_date} type=${meal_type}`);
        sendSuccess(res, data, 'Meal logged', 201);
    } catch (err) {
        next(err);
    }
}
