import { Router } from 'express';
import { subscribe } from '../controllers/payment.controller';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validate';
import { subscribeSchema } from '../validators/schemas';

const router = Router();

router.use(authenticate);

router.post('/subscribe', validate(subscribeSchema), subscribe);

export default router;
