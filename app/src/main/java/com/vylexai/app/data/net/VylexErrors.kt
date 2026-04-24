package com.vylexai.app.data.net

import java.io.IOException

/**
 * All non-recoverable coordinator errors surface as a single sealed type.
 *
 * Extends [IOException] so OkHttp propagates it through the interceptor chain
 * unchanged instead of wrapping it in a generic "canceled due to" IOException.
 */
sealed class VylexException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause) {
    /** JWT missing or rejected — caller should clear the token and re-authenticate. */
    class AuthExpired(cause: Throwable? = null) : VylexException("auth_expired", cause)

    /** Client tried to create a job but lacks BSAI to cover the cost. */
    class InsufficientFunds(cause: Throwable? = null) : VylexException("insufficient_bsai", cause)

    /** Validation or client-supplied bad request. */
    class BadRequest(detail: String, cause: Throwable? = null) : VylexException(detail, cause)

    /** Anything else — 5xx, network, timeouts. */
    class Unavailable(cause: Throwable? = null) : VylexException("unavailable", cause)
}
