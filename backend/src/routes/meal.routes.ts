import { Router } from 'express';
import { generateMealPlan, getMealPlan, swapMeal } from '../controllers/meal.controller';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validate';
import { generateMealPlanSchema, swapMealSchema } from '../validators/schemas';
import { aiLimiter } from '../middleware/rateLimiter';

const router = Router();

router.use(authenticate);

router.post('/generate-plan', aiLimiter, validate(generateMealPlanSchema), generateMealPlan);
router.get('/meal-plan', getMealPlan);
router.post('/swap-meal', aiLimiter, validate(swapMealSchema), swapMeal);

export default router;
