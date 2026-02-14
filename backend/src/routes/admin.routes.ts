import { Router } from 'express';
import { getStats } from '../controllers/admin.controller';
import { authenticate, authorize } from '../middleware/auth';

const router = Router();

router.use(authenticate);
router.use(authorize('admin'));

router.get('/stats', getStats);

export default router;
