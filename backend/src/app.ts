import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import morgan from 'morgan';
import { env } from './config/env';
import { logger } from './config/logger';
import { apiLimiter } from './middleware/rateLimiter';
import { errorHandler, notFoundHandler } from './middleware/errorHandler';
import routes from './routes';

const app = express();

// ── Security ───────────────────────────────────────────
app.use(helmet());
app.use(cors({
    origin: env.corsOrigin,
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization'],
    credentials: true,
    maxAge: 86400, // 24h preflight cache
}));

// ── Parsing ────────────────────────────────────────────
app.use(express.json({ limit: '1mb' }));
app.use(express.urlencoded({ extended: true, limit: '1mb' }));
app.use(compression());

// ── Logging ────────────────────────────────────────────
const morganFormat = env.isProduction ? 'combined' : 'dev';
app.use(morgan(morganFormat, {
    stream: { write: (msg: string) => logger.http(msg.trim()) },
    skip: (_req, res) => env.isProduction && res.statusCode < 400,
}));

// ── Rate Limiting ──────────────────────────────────────
app.use('/api', apiLimiter);

// ── Trust Proxy (for rate limiter behind reverse proxy) ─
app.set('trust proxy', 1);

// ── Routes ─────────────────────────────────────────────
app.use('/api', routes);

// ── 404 + Error Handling ───────────────────────────────
app.use(notFoundHandler);
app.use(errorHandler);

export default app;
