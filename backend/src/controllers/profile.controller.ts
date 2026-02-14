import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest, AppError } from '../types';
import { sendSuccess } from '../utils/helpers';
import { cacheService } from '../services/cache.service';

export async function getProfile(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;

        // Check cache
        const cacheKey = `profile:${userId}`;
        const cached = cacheService.get(cacheKey);
        if (cached) {
            sendSuccess(res, cached, 'Profile loaded (cached)');
            return;
        }

        const { data: profile, error } = await supabaseAdmin
            .from('profiles')
            .select('*')
            .eq('user_id', userId)
            .eq('is_deleted', false)
            .single();

        if (error || !profile) {
            throw new AppError('Profile not found', 404);
        }

        cacheService.set(cacheKey, profile, 300); // 5 min

        sendSuccess(res, profile, 'Profile loaded');
    } catch (err) {
        next(err);
    }
}

export async function updateProfile(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const updates = req.body;

        const { data: profile, error } = await supabaseAdmin
            .from('profiles')
            .update(updates)
            .eq('user_id', userId)
            .eq('is_deleted', false)
            .select()
            .single();

        if (error) {
            logger.error('Profile update failed', { error: error.message, userId });
            throw new AppError('Failed to update profile', 500);
        }

        if (!profile) {
            throw new AppError('Profile not found', 404);
        }

        // Invalidate cache
        cacheService.del(`profile:${userId}`);

        logger.info(`Profile updated for user=${userId}`);
        sendSuccess(res, profile, 'Profile updated');
    } catch (err) {
        next(err);
    }
}
