import { Response, NextFunction } from 'express';
import { supabaseAdmin } from '../config/database';
import { logger } from '../config/logger';
import { AuthRequest } from '../types';
import { sendSuccess } from '../utils/helpers';
import { paymentService } from '../services/payment.service';

export async function subscribe(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
        const userId = req.user!.id;
        const { plan_name } = req.body;

        // Validate plan exists
        const plan = paymentService.getPlan(plan_name);

        // Create Razorpay order
        const order = await paymentService.createOrder(plan_name, userId);

        // Create pending payment record
        const { error: paymentError } = await supabaseAdmin
            .from('payments')
            .insert({
                user_id: userId,
                amount: plan.amount / 100, // Convert paise to rupees
                currency: 'INR',
                status: 'pending',
                payment_gateway: 'razorpay',
                gateway_order_id: order.order_id as string,
                metadata: { plan_name },
            });

        if (paymentError) {
            logger.error('Failed to create payment record', { error: paymentError.message });
        }

        logger.info(`Subscription order created: user=${userId} plan=${plan_name}`);

        sendSuccess(res, {
            order,
            plan: {
                name: plan_name,
                amount_display: `₹${plan.amount / 100}`,
                duration_days: plan.duration_days,
                features: plan.features,
            },
        }, 'Payment order created');
    } catch (err) {
        next(err);
    }
}
