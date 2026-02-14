import winston from 'winston';
import { env } from './env';

const { combine, timestamp, printf, colorize, errors } = winston.format;

const devFormat = combine(
    colorize(),
    timestamp({ format: 'HH:mm:ss' }),
    errors({ stack: true }),
    printf(({ level, message, timestamp: ts, stack, ...meta }) => {
        const metaStr = Object.keys(meta).length ? ` ${JSON.stringify(meta)}` : '';
        if (stack) return `${ts} ${level}: ${message}\n${stack}${metaStr}`;
        return `${ts} ${level}: ${message}${metaStr}`;
    })
);

const prodFormat = combine(
    timestamp(),
    errors({ stack: true }),
    winston.format.json()
);

export const logger = winston.createLogger({
    level: env.logLevel,
    format: env.isProduction ? prodFormat : devFormat,
    defaultMeta: { service: 'dietapp-api' },
    transports: [
        new winston.transports.Console(),
        ...(env.isProduction
            ? [
                new winston.transports.File({ filename: 'logs/error.log', level: 'error', maxsize: 5242880, maxFiles: 5 }),
                new winston.transports.File({ filename: 'logs/combined.log', maxsize: 5242880, maxFiles: 5 }),
            ]
            : []),
    ],
    exceptionHandlers: [
        new winston.transports.Console(),
    ],
    rejectionHandlers: [
        new winston.transports.Console(),
    ],
});
