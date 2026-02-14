import { Response } from 'express';
import { ApiResponse } from '../types';

export function sendSuccess<T>(res: Response, data: T, message?: string, statusCode = 200): void {
    const response: ApiResponse<T> = {
        success: true,
        data,
        message,
    };
    res.status(statusCode).json(response);
}

export function sendError(res: Response, message: string, statusCode = 500, details?: unknown): void {
    const response: ApiResponse = {
        success: false,
        error: message,
        ...(details && process.env.NODE_ENV !== 'production' ? { data: details } : {}),
    };
    res.status(statusCode).json(response);
}

export function sendPaginated<T>(
    res: Response,
    data: T[],
    total: number,
    page: number,
    limit: number,
    message?: string
): void {
    const response: ApiResponse<T[]> = {
        success: true,
        data,
        message,
        meta: { page, limit, total },
    };
    res.status(200).json(response);
}

export function formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
}

export function todayDateString(): string {
    return formatDate(new Date());
}
