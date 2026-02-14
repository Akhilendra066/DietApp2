import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest, AppError, ChatMessage } from '../types';
import { sendSuccess } from '../utils/helpers';
import { aiService } from '../services/ai.service';
import { v4 as uuidv4 } from 'uuid';

export async function chat(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const { message, session_id } = req.body;
        const sessionId = session_id || uuidv4();

        // Get user profile for context
        const { data: profile, error: profileError } = await supabaseAdmin
            .from('profiles')
            .select('*')
            .eq('user_id', userId)
            .eq('is_deleted', false)
            .single();

        if (profileError || !profile) {
            throw new AppError('Profile not found. Complete onboarding first.', 404);
        }

        // Fetch conversation history for this session
        const { data: historyRows } = await supabaseAdmin
            .from('chat_history')
            .select('role, content')
            .eq('user_id', userId)
            .eq('session_id', sessionId)
            .eq('is_deleted', false)
            .order('created_at', { ascending: true })
            .limit(20);

        const history: ChatMessage[] = (historyRows || []).map((row) => ({
            role: row.role as ChatMessage['role'],
            content: row.content,
        }));

        // Call AI
        const aiResponse = await aiService.chat(profile, history, message);

        // Save user message + AI response
        const { error: insertError } = await supabaseAdmin
            .from('chat_history')
            .insert([
                {
                    user_id: userId,
                    session_id: sessionId,
                    role: 'user',
                    content: message,
                    content_type: 'text',
                    ai_model: 'gemini-2.0-flash',
                },
                {
                    user_id: userId,
                    session_id: sessionId,
                    role: 'assistant',
                    content: aiResponse,
                    content_type: 'text',
                    ai_model: 'gemini-2.0-flash',
                },
            ]);

        if (insertError) {
            logger.error('Failed to save chat history', { error: insertError.message });
            // Don't throw — still return the AI response
        }

        logger.info(`Chat: user=${userId} session=${sessionId}`);
        sendSuccess(res, {
            session_id: sessionId,
            response: aiResponse,
        }, 'Chat response generated');
    } catch (err) {
        next(err);
    }
}
