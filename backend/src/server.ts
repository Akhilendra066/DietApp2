import app from './app';
import { env } from './config/env';
import { logger } from './config/logger';
import { checkDatabaseHealth } from './config/database';

async function bootstrap(): Promise<void> {
    // Verify database connection before starting
    const dbHealthy = await checkDatabaseHealth();
    if (!dbHealthy) {
        logger.warn('Database health check failed — starting anyway (Supabase may be warming up)');
    } else {
        logger.info('Database connection verified');
    }

    const server = app.listen(env.port, () => {
        logger.info(`🚀 DietApp API running on port ${env.port} [${env.nodeEnv}]`);
        logger.info(`   Health: http://localhost:${env.port}/api/health`);
    });

    // Graceful shutdown
    const shutdown = (signal: string) => {
        logger.info(`${signal} received — shutting down gracefully`);
        server.close(() => {
            logger.info('HTTP server closed');
            process.exit(0);
        });
        // Force exit after 10s
        setTimeout(() => {
            logger.error('Forced shutdown after timeout');
            process.exit(1);
        }, 10000);
    };

    process.on('SIGTERM', () => shutdown('SIGTERM'));
    process.on('SIGINT', () => shutdown('SIGINT'));

    // Unhandled errors
    process.on('unhandledRejection', (reason) => {
        logger.error('Unhandled Rejection', { reason });
    });

    process.on('uncaughtException', (error) => {
        logger.error('Uncaught Exception', { error: error.message, stack: error.stack });
        process.exit(1);
    });
}

bootstrap().catch((err) => {
    logger.error('Failed to start server', { error: err });
    process.exit(1);
});
