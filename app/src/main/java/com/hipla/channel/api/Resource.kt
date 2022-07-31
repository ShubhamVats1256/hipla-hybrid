package com.hipla.channel.api

import timber.log.Timber

/**
 * A utility class used to wrap success and error results, relieving the developer of need to use callbacks when calling some API for example
 * that will respond with either onSuccess() or onFailure() method. Used in conjunction with coroutines allows for very concise synchronous code
 * to be written.
 * */
sealed class Resource<out T> {
    companion object {
        fun <T> error(exception: Throwable? = null) = ResourceError<T>(throwable = exception)
        fun <T> error(message: String) = ResourceError<T>(Exception(message))
        fun <T> error(error: ResourceError<*>) = ResourceError<T>(
            throwable = error.throwable,
            code = error.code,
            data = null
        )
    }
}

open class ResourceSuccess<out T>(val data: T) : Resource<T>()
open class ResourceError<out T>(
    val throwable: Throwable? = null,
    val code: Int = 0,
    val data: T? = null
) : Resource<T>()

typealias Status = Resource<Unit>

object StatusUnknown : ResourceError<Unit>()
object StatusSuccess : ResourceSuccess<Unit>(Unit)
object StatusError : ResourceError<Unit>()

fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is ResourceSuccess -> ResourceSuccess(
            transform(data)
        )
        is ResourceError -> ResourceError(
            throwable,
            code,
            data?.let { transform(it) })
    }
}

suspend fun <T, R> Resource<T>.flatMap(transform: suspend (T) -> R): R {
    return when (this) {
        is ResourceSuccess -> transform(data)
        is ResourceError -> throw throwable ?: Throwable("Throwable is null !")
    }
}

inline fun Status.onSuccess(block: () -> Unit) {
    if (this is ResourceSuccess) {
        block()
    }
}

inline fun Status.onError(block: () -> Unit) {
    if (this is ResourceError) {
        block()
    }
}

fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> {
    if (this is ResourceSuccess) {
        block(data)
    }
    return this
}

fun <T> Resource<T>.unwrap(): T? {
    return if (this is ResourceSuccess) {
        this.data
    } else {
        null
    }
}

fun <T> Resource<T>.unwrapThrowing(): T {
    when (this) {
        is ResourceSuccess -> {
            return this.data
        }
        is ResourceError -> {
            throw this.throwable ?: Exception("Unwrap failed !")
        }
    }
}

fun <T> Resource<T>.asStatus(): Status {
    return when (this) {
        is ResourceSuccess -> StatusSuccess
        is ResourceError -> StatusError
    }
}

fun <T> Resource<T>.onComplete(
    onSuccess: (result: T) -> Unit,
    onError: ((errorCode: Int?, throwable: Throwable?) -> Unit)?
) {
    ifSuccessful {
        onSuccess(it)
    }
    ifError {
        onError?.invoke(it.code, it.throwable)
    }
}


fun <T> Resource<T>.ifSuccessful(callback: (T) -> Unit): Resource<T>? {
    return if (this is ResourceSuccess) {
        callback(data)
        this
    } else {
        null
    }
}

suspend fun <T> Resource<T>.ifSuccessfulSuspend(callback: suspend (T) -> Unit): Resource<T>? {
    return if (this is ResourceSuccess) {
        callback(data)
        this
    } else {
        null
    }
}

fun <T> Resource<T>.ifError(callback: (ResourceError<T>) -> Unit): Resource<T> {
    if (this is ResourceError) {
        callback(this)
    }
    return this
}

suspend fun <T> Resource<T>.ifErrorSuspend(callback: suspend (ResourceError<T>) -> Unit): Resource<T> {
    if (this is ResourceError) {
        callback(this)
    }
    return this
}

inline fun <T> resourceCatching(block: () -> T): Resource<T> {
    return try {
        ResourceSuccess(block())
    } catch (e: Throwable) {
        Resource.error(e)
    }
}

fun List<Status>.zip(): Status {
    forEach {
        if (it is StatusError) {
            return it
        }
    }
    return StatusSuccess
}