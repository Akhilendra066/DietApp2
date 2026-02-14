package com.example.dietapp.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import com.example.dietapp.domain.util.Resource

/**
 * Offline-first caching strategy.
 *
 * 1. Emit cached data with Loading state
 * 2. Try network fetch
 * 3. On success: save to cache, emit Success
 * 4. On failure: emit Error with cached data
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline onFetchFailed: (Throwable) -> Unit = { }
): Flow<Resource<ResultType>> = flow {

    // Step 1: Emit cached data as Loading
    val data = query().first()
    emit(Resource.Loading(data))

    if (shouldFetch(data)) {
        try {
            // Step 2: Fetch from network
            val fetchResult = fetch()

            // Step 3: Save to cache and emit Success
            saveFetchResult(fetchResult)
            emitAll(query().map { Resource.Success(it) })
        } catch (throwable: Throwable) {
            // Step 4: Emit Error with cached data
            onFetchFailed(throwable)
            emitAll(query().map {
                Resource.Error(
                    message = throwable.localizedMessage ?: "Unknown error",
                    data = it,
                    throwable = throwable
                )
            })
        }
    } else {
        // Cache is fresh, emit Success
        emitAll(query().map { Resource.Success(it) })
    }
}
