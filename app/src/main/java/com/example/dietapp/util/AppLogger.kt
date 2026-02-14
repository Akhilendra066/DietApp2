package com.example.dietapp.util

import timber.log.Timber

/**
 * App-level logging utility wrapping Timber.
 * Provides structured logging with tags.
 */
object AppLogger {

    private const val DEFAULT_TAG = "DietApp"

    fun d(message: String, tag: String = DEFAULT_TAG) {
        Timber.tag(tag).d(message)
    }

    fun i(message: String, tag: String = DEFAULT_TAG) {
        Timber.tag(tag).i(message)
    }

    fun w(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).w(throwable, message)
        } else {
            Timber.tag(tag).w(message)
        }
    }

    fun e(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
        } else {
            Timber.tag(tag).e(message)
        }
    }

    // Convenience tags for different layers
    object Tags {
        const val NETWORK = "Network"
        const val DATABASE = "Database"
        const val REPOSITORY = "Repository"
        const val VIEWMODEL = "ViewModel"
        const val UI = "UI"
        const val SYNC = "Sync"
        const val AUTH = "Auth"
    }
}
