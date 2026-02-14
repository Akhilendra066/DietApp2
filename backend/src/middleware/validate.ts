import { Request, Response, NextFunction } from 'express';
import { ZodSchema, ZodError } from 'zod';
import { AppError } from '../types';

type RequestField = 'body' | 'query' | 'params';

export function validate(schema: ZodSchema, field: RequestField = 'body') {
    return (req: Request, _res: Response, next: NextFunction): void => {
        try {
            const parsed = schema.parse(req[field]);
            // Replace with parsed/coerced values
            (req as unknown as Record<string, unknown>)[field] = parsed;
            next();
        } catch (err) {
            if (err instanceof ZodError) {
                const messages = err.errors.map((e) => `${e.path.join('.')}: ${e.message}`).join('; ');
                return next(new AppError(`Validation failed: ${messages}`, 400, err.errors));
            }
            next(err);
        }
    };
}
