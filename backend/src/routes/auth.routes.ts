import { Router } from 'express';
import { signup, login } from '../controllers/auth.controller';
import { validate } from '../middleware/validate';
import { signupSchema, loginSchema } from '../validators/schemas';
import { authLimiter } from '../middleware/rateLimiter';

const router = Router();

router.post('/signup', authLimiter, validate(signupSchema), signup);
router.post('/login', authLimiter, validate(loginSchema), login);

export default router;
