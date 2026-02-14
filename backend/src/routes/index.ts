import { Router, Request, Response } from 'express';
import { checkDatabaseHealth } from '../config/database';

import authRoutes from './auth.routes';
import profileRoutes from './profile.routes';
import mealRoutes from './meal.routes';
import trackingRoutes from './tracking.routes';
import chatRoutes from './chat.routes';
import groceryRoutes from './grocery.routes';
import paymentRoutes from './payment.routes';
import adminRoutes from './admin.routes';

const router = Router();

// Health check
router.get('/health', async (_req: Request, res: Response) => {
    const dbHealthy = await checkDatabaseHealth();
    const status = dbHealthy ? 200 : 503;
    res.status(status).json({
        success: dbHealthy,
        status: dbHealthy ? 'healthy' : 'degraded',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        services: {
            database: dbHealthy ? 'connected' : 'disconnected',
        },
    });
});

// Mount routes
router.use('/auth', authRoutes);
router.use('/profile', profileRoutes);
router.use('/meals', mealRoutes);
router.use('/tracking', trackingRoutes);
router.use('/chat', chatRoutes);
router.use('/grocery', groceryRoutes);
router.use('/payments', paymentRoutes);
router.use('/admin', adminRoutes);

export default router;
