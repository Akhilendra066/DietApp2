import { Router } from 'express';
import { getGroceryList } from '../controllers/grocery.controller';
import { authenticate } from '../middleware/auth';

const router = Router();

router.use(authenticate);

router.get('/', getGroceryList);

export default router;
