import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest, AppError, MealPlanData } from '../types';
import { sendSuccess, todayDateString } from '../utils/helpers';
import { aiService } from '../services/ai.service';
import { cacheService } from '../services/cache.service';

export async function getGroceryList(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const date = (req.query.date as string) || todayDateString();

        // Check for existing grocery list
        const cacheKey = `grocery:${userId}:${date}`;
        const cached = cacheService.get(cacheKey);
        if (cached) {
            sendSuccess(res, cached, 'Grocery list loaded (cached)');
            return;
        }

        // Check DB for existing grocery list linked to today's meal plan
        const { data: existingList } = await supabaseAdmin
            .from('grocery_lists')
            .select('*')
            .eq('user_id', userId)
            .eq('is_deleted', false)
            .order('created_at', { ascending: false })
            .limit(1);

        if (existingList && existingList.length > 0) {
            const list = existingList[0];
            cacheService.set(cacheKey, list, 600); // 10 min
            sendSuccess(res, list, 'Grocery list loaded');
            return;
        }

        // Get latest meal plan for the date
        const { data: plans } = await supabaseAdmin
            .from('meal_plans')
            .select('*')
            .eq('user_id', userId)
            .eq('plan_date', date)
            .eq('is_deleted', false)
            .order('created_at', { ascending: false })
            .limit(1);

        if (!plans || plans.length === 0) {
            throw new AppError('No meal plan found. Generate a meal plan first.', 404);
        }

        const mealPlanData = plans[0].plan_data as MealPlanData;
        const groceryItems = await aiService.generateGroceryList(mealPlanData);

        // Save to DB
        const totalCost = Array.isArray(groceryItems)
            ? (groceryItems as Array<{ estimated_price_inr?: number }>).reduce((sum, item) => sum + (item.estimated_price_inr || 0), 0)
            : 0;

        const { data: saved, error } = await supabaseAdmin
            .from('grocery_lists')
            .insert({
                user_id: userId,
                meal_plan_id: plans[0].id,
                title: `Grocery List - ${date}`,
                items: groceryItems,
                total_estimated_cost: totalCost,
            })
            .select()
            .single();

        if (error) {
            logger.error('Failed to save grocery list', { error: error.message });
            // Return the AI-generated list anyway
            sendSuccess(res, { items: groceryItems, total_estimated_cost: totalCost }, 'Grocery list generated');
            return;
        }

        cacheService.set(cacheKey, saved, 600);

        logger.info(`Grocery list generated for user=${userId} date=${date}`);
        sendSuccess(res, saved, 'Grocery list generated', 201);
    } catch (err) {
        next(err);
    }
}
