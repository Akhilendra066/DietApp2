import NodeCache from 'node-cache';
import { logger } from '../config/logger';

class CacheService {
    private cache: NodeCache;

    constructor() {
        this.cache = new NodeCache({
            stdTTL: 300,       // 5 minutes default
            checkperiod: 60,   // Check for expired keys every 60s
            useClones: false,  // Performance — return refs
        });

        this.cache.on('expired', (key: string) => {
            logger.debug(`Cache key expired: ${key}`);
        });
    }

    get<T>(key: string): T | undefined {
        return this.cache.get<T>(key);
    }

    set<T>(key: string, value: T, ttlSeconds?: number): boolean {
        return ttlSeconds ? this.cache.set(key, value, ttlSeconds) : this.cache.set(key, value);
    }

    del(key: string): number {
        return this.cache.del(key);
    }

    delByPrefix(prefix: string): number {
        const keys = this.cache.keys().filter((k) => k.startsWith(prefix));
        return this.cache.del(keys);
    }

    flush(): void {
        this.cache.flushAll();
        logger.info('Cache flushed');
    }

    stats() {
        return this.cache.getStats();
    }

    // Generate deterministic cache keys
    static key(...parts: (string | number | undefined)[]): string {
        return parts.filter(Boolean).join(':');
    }
}

export const cacheService = new CacheService();
