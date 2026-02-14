package com.example.dietapp.domain.usecase

import com.example.dietapp.domain.model.DailyNutritionSummary
import com.example.dietapp.domain.model.DietLog
import com.example.dietapp.domain.model.User
import com.example.dietapp.domain.repository.UserRepository
import com.example.dietapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Resource<User>> {
        return userRepository.getCurrentUser()
    }
}

class SaveUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Resource<Long> {
        return userRepository.saveUser(user)
    }
}

class LogDietUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long, dietLog: DietLog): Resource<Long> {
        return userRepository.logDiet(userId, dietLog)
    }
}

class GetDailyNutritionUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: Long, date: String): Flow<Resource<DailyNutritionSummary>> {
        return userRepository.getDailyNutritionSummary(userId, date)
    }
}

class SyncDietLogsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return userRepository.syncDietLogs()
    }
}
