import { Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { env } from '../config/env';
import { supabaseAdmin } from '../config/database';
import { AuthRequest, AuthUser, AppError } from '../types';
import { logger } from '../config/logger';

export async function authenticate(req: AuthRequest, _res: Response, next: NextFunction): Promise<void> {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith('Bearer ')) {
            throw new AppError('Missing or invalid Authorization header', 401);
        }

        const token = authHeader.split(' ')[1];

        // Verify the Supabase JWT
        let decoded: jwt.JwtPayload;
        try {
            decoded = jwt.verify(token, env.supabaseJwtSecret) as jwt.JwtPayload;
        } catch (err) {
            if (err instanceof jwt.TokenExpiredError) {
                throw new AppError('Token has expired', 401);
            }
            throw new AppError('Invalid token', 401);
        }

        const userId = decoded.sub;
        if (!userId) {
            throw new AppError('Invalid token payload', 401);
        }

        // Look up the user in our public.users table
        const { data: user, error } = await supabaseAdmin
            .from('users')
            .select('id, email, role, is_active, is_deleted')
            .eq('id', userId)
            .single();

        if (error || !user) {
            throw new AppError('User not found', 401);
        }

        if (!user.is_active || user.is_deleted) {
            throw new AppError('Account is deactivated', 403);
        }

        req.user = {
            id: user.id,
            email: user.email,
            role: user.role,
        } as AuthUser;

        req.accessToken = token;

        next();
    } catch (err) {
        next(err);
    }
}

// Role-based authorization
export function authorize(...roles: string[]) {
    return (req: AuthRequest, _res: Response, next: NextFunction): void => {
        if (!req.user) {
            return next(new AppError('Not authenticated', 401));
        }
        if (!roles.includes(req.user.role)) {
            return next(new AppError('Insufficient permissions', 403));
        }
        next();
    };
}

// Optional auth — doesn't fail if no token, just sets req.user if present
export async function optionalAuth(req: AuthRequest, _res: Response, next: NextFunction): Promise<void> {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith('Bearer ')) {
            return next();
        }

        const token = authHeader.split(' ')[1];
        try {
            const decoded = jwt.verify(token, env.supabaseJwtSecret) as jwt.JwtPayload;
            if (decoded.sub) {
                const { data: user } = await supabaseAdmin
                    .from('users')
                    .select('id, email, role')
                    .eq('id', decoded.sub)
                    .eq('is_active', true)
                    .eq('is_deleted', false)
                    .single();

                if (user) {
                    req.user = { id: user.id, email: user.email, role: user.role };
                    req.accessToken = token;
                }
            }
        } catch {
            // Token invalid — continue as unauthenticated
            logger.debug('Optional auth: invalid token, continuing unauthenticated');
        }

        next();
    } catch (err) {
        next(err);
    }
}
