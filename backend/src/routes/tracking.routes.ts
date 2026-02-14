import { Router } from 'express';
import { logWeight, logWater, logMealComplete } from '../controllers/tracking.controller';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validate';
import { weightLogSchema, waterLogSchema, mealCompleteSchema } from '../validators/schemas';

const router = Router();

router.use(authenticate);

router.post('/weight', validate(weightLogSchema), logWeight);
router.post('/water', validate(waterLogSchema), logWater);
router.post('/meal-complete', validate(mealCompleteSchema), logMealComplete);

export default router;
