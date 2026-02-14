import { Router } from 'express';
import { chat } from '../controllers/chat.controller';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validate';
import { chatSchema } from '../validators/schemas';
import { aiLimiter } from '../middleware/rateLimiter';

const router = Router();

router.use(authenticate);

router.post('/', aiLimiter, validate(chatSchema), chat);

export default router;
