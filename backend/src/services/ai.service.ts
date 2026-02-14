import { GoogleGenerativeAI, GenerativeModel, Content } from '@google/generative-ai';
import { env } from '../config/env';
import { logger } from '../config/logger';
import { cacheService } from './cache.service';
import { AppError, MealPlanData, Profile, ChatMessage } from '../types';
import crypto from 'crypto';

class AIService {
    private model: GenerativeModel;

    constructor() {
        const genAI = new GoogleGenerativeAI(env.geminiApiKey);
        this.model = genAI.getGenerativeModel({ model: env.geminiModel });
    }

    // ── Generate Meal Plan ─────────────────────────────────
    async generateMealPlan(profile: Profile, date: string, preferences?: string): Promise<MealPlanData> {
        const cacheKey = this.buildCacheKey('meal_plan', profile.user_id, date, profile.dietary_preference, profile.goal);
        const cached = cacheService.get<MealPlanData>(cacheKey);
        if (cached) {
            logger.info(`AI meal plan cache hit for user=${profile.user_id} date=${date}`);
            return cached;
        }

        const prompt = this.buildMealPlanPrompt(profile, date, preferences);
        const raw = await this.callGemini(prompt);
        const plan = this.parseMealPlanResponse(raw);

        cacheService.set(cacheKey, plan, 3600); // 1 hour
        return plan;
    }

    // ── Swap Meal ──────────────────────────────────────────
    async swapMeal(
        profile: Profile,
        currentMealPlan: MealPlanData,
        mealIndex: number,
        reason?: string
    ): Promise<MealPlanData> {
        const oldMeal = currentMealPlan.meals[mealIndex];
        if (!oldMeal) {
            throw new AppError('Invalid meal index', 400);
        }

        const prompt = `You are an expert Indian dietitian AI.

A user wants to swap a meal from their plan. Generate a SINGLE replacement meal.

User profile:
- Dietary preference: ${profile.dietary_preference}
- Goal: ${profile.goal}
- Allergies: ${(profile.allergies || []).join(', ') || 'None'}
- Target calories: ${profile.target_calories || 'Not set'}

Current meal being replaced:
- Type: ${oldMeal.meal_type}
- Name: ${oldMeal.name}
- Calories: ${oldMeal.calories}
${reason ? `- Reason for swap: ${reason}` : ''}

Requirements:
- Similar calorie range (±50 kcal)
- Respects dietary preference
- Avoids allergens
- Indian cuisine preferred

Respond ONLY with a JSON object (no markdown, no explanation):
{
  "meal_type": "${oldMeal.meal_type}",
  "name": "...",
  "name_hindi": "...",
  "calories": ...,
  "protein_g": ...,
  "carbs_g": ...,
  "fat_g": ...,
  "fiber_g": ...,
  "ingredients": ["..."],
  "prep_time_min": ...,
  "is_vegetarian": true/false
}`;

        const raw = await this.callGemini(prompt);
        const newMeal = this.parseJSON(raw);

        const updatedPlan = { ...currentMealPlan };
        updatedPlan.meals = [...currentMealPlan.meals];
        updatedPlan.meals[mealIndex] = newMeal as unknown as MealPlanData['meals'][number];

        // Recalculate totals
        updatedPlan.total_calories = updatedPlan.meals.reduce((s, m) => s + m.calories, 0);
        updatedPlan.total_protein_g = updatedPlan.meals.reduce((s, m) => s + m.protein_g, 0);
        updatedPlan.total_carbs_g = updatedPlan.meals.reduce((s, m) => s + m.carbs_g, 0);
        updatedPlan.total_fat_g = updatedPlan.meals.reduce((s, m) => s + m.fat_g, 0);
        updatedPlan.total_fiber_g = updatedPlan.meals.reduce((s, m) => s + m.fiber_g, 0);

        return updatedPlan;
    }

    // ── Chat ───────────────────────────────────────────────
    async chat(profile: Profile, history: ChatMessage[], userMessage: string): Promise<string> {
        const systemContext = `You are NutriBot, a friendly and knowledgeable Indian diet planner AI assistant.

User profile:
- Name: ${profile.full_name}
- Dietary preference: ${profile.dietary_preference}
- Goal: ${profile.goal}
- Activity level: ${profile.activity_level}
- Allergies: ${(profile.allergies || []).join(', ') || 'None'}
- Height: ${profile.height_cm || 'Not set'} cm
- Weight: ${profile.weight_kg || 'Not set'} kg
- Target weight: ${profile.target_weight_kg || 'Not set'} kg

Rules:
- Be encouraging and supportive
- Give practical, actionable Indian diet advice
- Reference specific Indian foods and recipes
- If asked about medical conditions, recommend consulting a doctor
- Keep responses concise (under 300 words)
- Use Hindi food names where appropriate`;

        const contents: Content[] = [
            { role: 'user', parts: [{ text: systemContext }] },
            { role: 'model', parts: [{ text: 'Understood! I am NutriBot, ready to help with personalized Indian diet advice.' }] },
        ];

        // Add conversation history (last 10 messages)
        const recent = history.slice(-10);
        for (const msg of recent) {
            contents.push({
                role: msg.role === 'assistant' ? 'model' : 'user',
                parts: [{ text: msg.content }],
            });
        }

        contents.push({ role: 'user', parts: [{ text: userMessage }] });

        const result = await this.model.generateContent({ contents });
        const response = result.response.text();

        if (!response) {
            throw new AppError('AI returned empty response', 502);
        }

        return response;
    }

    // ── Generate Grocery List from Meal Plan ───────────────
    async generateGroceryList(mealPlan: MealPlanData, days: number = 1): Promise<unknown> {
        const cacheKey = this.buildCacheKey('grocery', crypto.createHash('md5').update(JSON.stringify(mealPlan)).digest('hex'));
        const cached = cacheService.get(cacheKey);
        if (cached) return cached;

        const prompt = `You are a grocery planning assistant for Indian households.

Given this ${days}-day meal plan, generate a consolidated grocery list.

Meals:
${mealPlan.meals.map((m) => `- ${m.name}: ${(m.ingredients || []).join(', ')}`).join('\n')}

Respond ONLY with a JSON array (no markdown, no explanation):
[
  {
    "name": "...",
    "name_hindi": "...",
    "quantity": "...",
    "unit": "kg/g/L/pieces/pack",
    "category": "vegetables/fruits/grains/dairy/spices/oil/protein/other",
    "estimated_price_inr": ...
  }
]

Consolidate duplicate items. Include estimated Indian market prices in INR.`;

        const raw = await this.callGemini(prompt);
        const list = this.parseJSON(raw);

        cacheService.set(cacheKey, list, 1800); // 30 min
        return list;
    }

    // ── Private Helpers ────────────────────────────────────

    private buildMealPlanPrompt(profile: Profile, date: string, preferences?: string): string {
        return `You are an expert Indian dietitian. Generate a complete daily meal plan.

User profile:
- Dietary preference: ${profile.dietary_preference}
- Goal: ${profile.goal}
- Activity level: ${profile.activity_level}
- Height: ${profile.height_cm || 'Not provided'} cm
- Weight: ${profile.weight_kg || 'Not provided'} kg
- Target weight: ${profile.target_weight_kg || 'Not provided'} kg
- Target daily calories: ${profile.target_calories || 'Auto-calculate based on profile'}
- Allergies: ${(profile.allergies || []).join(', ') || 'None'}
- Medical conditions: ${(profile.medical_conditions || []).join(', ') || 'None'}
- Preferred cuisines: ${(profile.preferred_cuisines || []).join(', ') || 'Indian'}
${preferences ? `- Additional preferences: ${preferences}` : ''}
- Date: ${date}

Generate a plan with 5-6 meals: breakfast, morning_snack, lunch, evening_snack, dinner (optionally late_night).

Respond ONLY with a JSON object (no markdown, no explanation):
{
  "total_calories": ...,
  "total_protein_g": ...,
  "total_carbs_g": ...,
  "total_fat_g": ...,
  "total_fiber_g": ...,
  "meals": [
    {
      "meal_type": "breakfast|morning_snack|lunch|evening_snack|dinner|late_night",
      "name": "...",
      "name_hindi": "...",
      "calories": ...,
      "protein_g": ...,
      "carbs_g": ...,
      "fat_g": ...,
      "fiber_g": ...,
      "ingredients": ["..."],
      "prep_time_min": ...,
      "is_vegetarian": true/false
    }
  ],
  "notes": "Brief dietary notes or tips for the day"
}`;
    }

    private async callGemini(prompt: string): Promise<string> {
        try {
            logger.info('Calling Gemini AI...');
            const result = await this.model.generateContent(prompt);
            const text = result.response.text();
            if (!text) throw new AppError('AI returned empty response', 502);
            logger.info('Gemini AI response received');
            return text;
        } catch (err) {
            if (err instanceof AppError) throw err;
            logger.error('Gemini AI call failed', { error: err });
            throw new AppError('AI service temporarily unavailable', 503);
        }
    }

    private parseMealPlanResponse(raw: string): MealPlanData {
        const plan = this.parseJSON(raw) as unknown as MealPlanData;
        if (!plan.meals || !Array.isArray(plan.meals) || plan.meals.length === 0) {
            throw new AppError('AI returned invalid meal plan structure', 502);
        }
        return plan;
    }

    private parseJSON(raw: string): Record<string, unknown> {
        // Strip markdown code fences if present
        let cleaned = raw.trim();
        if (cleaned.startsWith('```')) {
            cleaned = cleaned.replace(/^```(?:json)?\n?/, '').replace(/\n?```$/, '');
        }
        try {
            return JSON.parse(cleaned);
        } catch {
            logger.error('Failed to parse AI JSON response', { raw: cleaned.substring(0, 500) });
            throw new AppError('AI returned invalid JSON', 502);
        }
    }

    private buildCacheKey(...parts: (string | number | undefined)[]): string {
        return `ai:${parts.filter(Boolean).join(':')}`;
    }
}

export const aiService = new AIService();
