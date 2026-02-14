package com.example.dietapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.dietapp.domain.repository.UserRepository
import com.example.dietapp.domain.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker started")

        return try {
            val syncResult = userRepository.syncDietLogs()
            when (syncResult) {
                is Resource.Success -> {
                    Timber.d("SyncWorker completed successfully")
                    Result.success()
                }
                is Resource.Error -> {
                    Timber.w("SyncWorker failed: ${syncResult.message}")
                    if (runAttemptCount < MAX_RETRIES) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is Resource.Loading -> Result.success()
            }
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker exception")
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "diet_sync_worker"
        private const val MAX_RETRIES = 3

        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )

            Timber.d("Periodic sync worker enqueued")
        }
    }
}
