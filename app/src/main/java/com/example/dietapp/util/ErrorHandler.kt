package com.example.dietapp.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor() {

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Connection timed out. Please try again."
            is IOException -> "Network error occurred. Please try again."
            is HttpException -> getHttpErrorMessage(throwable.code())
            else -> throwable.localizedMessage ?: "An unexpected error occurred."
        }
    }

    private fun getHttpErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Bad request. Please check your input."
            401 -> "Unauthorized. Please log in again."
            403 -> "Access denied."
            404 -> "Resource not found."
            408 -> "Request timed out."
            429 -> "Too many requests. Please wait a moment."
            in 500..599 -> "Server error. Please try again later."
            else -> "HTTP error: $code"
        }
    }
}
