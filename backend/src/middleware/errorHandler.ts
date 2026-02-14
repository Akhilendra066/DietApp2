import { Request, Response, NextFunction } from 'express';
import { AppError } from '../types';
import { logger } from '../config/logger';
import { env } from '../config/env';

export function errorHandler(err: Error, _req: Request, res: Response, _next: NextFunction): void {
    // AppError — operational, expected
    if (err instanceof AppError) {
        logger.warn(`AppError [${err.statusCode}]: ${err.message}`);
        res.status(err.statusCode).json({
            success: false,
            error: err.message,
            ...(err.details && !env.isProduction ? { details: err.details } : {}),
        });
        return;
    }

    // Unexpected error
    logger.error('Unhandled error', { error: err.message, stack: err.stack });

    res.status(500).json({
        success: false,
        error: env.isProduction ? 'Internal server error' : err.message,
        ...(!env.isProduction ? { stack: err.stack } : {}),
    });
}

export function notFoundHandler(_req: Request, _res: Response, next: NextFunction): void {
    next(new AppError('Route not found', 404));
}
