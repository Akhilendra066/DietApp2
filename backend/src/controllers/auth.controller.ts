import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest, AppError } from '../types';
import { sendSuccess } from '../utils/helpers';

export async function signup(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const { email, password, full_name, phone } = req.body;

        // Create user in Supabase Auth
        const { data: authData, error: authError } = await supabaseAdmin.auth.admin.createUser({
            email,
            password,
            email_confirm: true,
            user_metadata: { full_name },
        });

        if (authError) {
            if (authError.message.includes('already registered')) {
                throw new AppError('Email already registered', 409);
            }
            logger.error('Supabase auth signup failed', { error: authError.message });
            throw new AppError('Signup failed', 500);
        }

        const userId = authData.user.id;

        // Insert into public.users
        const { error: userError } = await supabaseAdmin
            .from('users')
            .insert({
                id: userId,
                email,
                phone: phone || null,
                role: 'user',
            });

        if (userError) {
            // Rollback: delete auth user
            await supabaseAdmin.auth.admin.deleteUser(userId);
            logger.error('Failed to create public user row', { error: userError.message });
            throw new AppError('Signup failed', 500);
        }

        // The handle_new_user trigger auto-creates profile + trial subscription

        // Sign in to get tokens
        const { data: signInData, error: signInError } = await supabaseAdmin.auth.signInWithPassword({
            email,
            password,
        });

        if (signInError || !signInData.session) {
            throw new AppError('Account created but login failed. Please log in manually.', 201);
        }

        logger.info(`New user signed up: ${email}`);

        sendSuccess(res, {
            user: {
                id: userId,
                email,
                full_name,
            },
            session: {
                access_token: signInData.session.access_token,
                refresh_token: signInData.session.refresh_token,
                expires_at: signInData.session.expires_at,
            },
        }, 'Signup successful', 201);
    } catch (err) {
        next(err);
    }
}

export async function login(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const { email, password } = req.body;

        const { data, error } = await supabaseAdmin.auth.signInWithPassword({
            email,
            password,
        });

        if (error) {
            if (error.message.includes('Invalid login credentials')) {
                throw new AppError('Invalid email or password', 401);
            }
            logger.error('Login failed', { error: error.message });
            throw new AppError('Login failed', 500);
        }

        if (!data.session) {
            throw new AppError('Login failed — no session', 500);
        }

        // Update last_login_at
        await supabaseAdmin
            .from('users')
            .update({ last_login_at: new Date().toISOString() })
            .eq('id', data.user.id);

        logger.info(`User logged in: ${email}`);

        sendSuccess(res, {
            user: {
                id: data.user.id,
                email: data.user.email,
            },
            session: {
                access_token: data.session.access_token,
                refresh_token: data.session.refresh_token,
                expires_at: data.session.expires_at,
            },
        }, 'Login successful');
    } catch (err) {
        next(err);
    }
}
