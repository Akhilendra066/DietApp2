import Razorpay from 'razorpay';
import crypto from 'crypto';
import { env } from '../config/env';
import { logger } from '../config/logger';
import { AppError } from '../types';

const PLANS: Record<string, { amount: number; currency: string; duration_days: number; features: Record<string, unknown> }> = {
    basic: {
        amount: 29900, // ₹299 in paise
        currency: 'INR',
        duration_days: 30,
        features: {
            ai_meal_plans_per_day: 3,
            chat_messages_per_day: 20,
            dietitian_bookings: false,
            lab_report_analysis: false,
            ad_free: true,
        },
    },
    premium: {
        amount: 59900, // ₹599
        currency: 'INR',
        duration_days: 30,
        features: {
            ai_meal_plans_per_day: 10,
            chat_messages_per_day: 100,
            dietitian_bookings: true,
            lab_report_analysis: true,
            ad_free: true,
        },
    },
    family: {
        amount: 99900, // ₹999
        currency: 'INR',
        duration_days: 30,
        features: {
            ai_meal_plans_per_day: 20,
            chat_messages_per_day: -1, // unlimited
            dietitian_bookings: true,
            lab_report_analysis: true,
            ad_free: true,
        },
    },
};

class PaymentService {
    private razorpay: InstanceType<typeof Razorpay>;

    constructor() {
        this.razorpay = new Razorpay({
            key_id: env.razorpayKeyId,
            key_secret: env.razorpayKeySecret,
        });
    }

    getPlan(planName: string) {
        const plan = PLANS[planName];
        if (!plan) throw new AppError(`Unknown plan: ${planName}`, 400);
        return { name: planName, ...plan };
    }

    async createOrder(planName: string, userId: string): Promise<Record<string, unknown>> {
        const plan = this.getPlan(planName);

        try {
            const order = await this.razorpay.orders.create({
                amount: plan.amount,
                currency: plan.currency,
                receipt: `sub_${userId}_${Date.now()}`,
                notes: {
                    user_id: userId,
                    plan_name: planName,
                },
            });

            logger.info(`Razorpay order created: ${order.id}`, { userId, planName });
            return {
                order_id: order.id,
                amount: order.amount,
                currency: order.currency,
                plan_name: planName,
                key_id: env.razorpayKeyId,
            };
        } catch (err) {
            logger.error('Razorpay order creation failed', { error: err });
            throw new AppError('Payment service unavailable', 503);
        }
    }

    verifyPaymentSignature(orderId: string, paymentId: string, signature: string): boolean {
        const body = `${orderId}|${paymentId}`;
        const expectedSignature = crypto
            .createHmac('sha256', env.razorpayKeySecret)
            .update(body)
            .digest('hex');
        return crypto.timingSafeEqual(Buffer.from(expectedSignature), Buffer.from(signature));
    }
}

export const paymentService = new PaymentService();
