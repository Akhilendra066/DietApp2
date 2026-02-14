import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest } from '../types';
import { sendSuccess } from '../utils/helpers';

export async function getStats(_req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        // Parallel aggregation queries
        const [
            usersResult,
            activeUsersResult,
            mealPlansResult,
            subscriptionsResult,
            revenueResult,
            dailyLogsResult,
        ] = await Promise.all([
            // Total users
            supabaseAdmin
                .from('users')
                .select('id', { count: 'exact', head: true })
                .eq('is_deleted', false),

            // Active users (logged in within 7 days)
            supabaseAdmin
                .from('users')
                .select('id', { count: 'exact', head: true })
                .eq('is_deleted', false)
                .gte('last_login_at', new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString()),

            // Total meal plans generated
            supabaseAdmin
                .from('meal_plans')
                .select('id', { count: 'exact', head: true })
                .eq('is_deleted', false)
                .eq('is_ai_generated', true),

            // Active subscriptions
            supabaseAdmin
                .from('subscriptions')
                .select('id', { count: 'exact', head: true })
                .eq('status', 'active')
                .eq('is_deleted', false),

            // Total revenue
            supabaseAdmin
                .from('payments')
                .select('amount')
                .eq('status', 'completed')
                .eq('is_deleted', false),

            // Total daily logs this month
            supabaseAdmin
                .from('daily_logs')
                .select('id', { count: 'exact', head: true })
                .eq('is_deleted', false)
                .gte('log_date', new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0]),
        ]);

        const totalRevenue = (revenueResult.data || []).reduce((sum, row) => sum + (parseFloat(row.amount) || 0), 0);

        const stats = {
            total_users: usersResult.count || 0,
            active_users_7d: activeUsersResult.count || 0,
            total_ai_meal_plans: mealPlansResult.count || 0,
            active_subscriptions: subscriptionsResult.count || 0,
            total_revenue_inr: totalRevenue,
            daily_logs_this_month: dailyLogsResult.count || 0,
            generated_at: new Date().toISOString(),
        };

        logger.info('Admin stats fetched');
        sendSuccess(res, stats, 'Dashboard stats');
    } catch (err) {
        next(err);
    }
}
