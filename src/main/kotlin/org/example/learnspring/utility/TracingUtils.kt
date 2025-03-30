package org.example.learnspring.utility

import io.micrometer.tracing.Span
import io.micrometer.tracing.Tracer

inline fun <T> Tracer.withSpan(spanName: String, block: (Span) -> T): T {
    val span = this.nextSpan().name(spanName).start()
    try {
        return block(span)
    } catch (ex: Exception) {
        span.tag("error", ex.message ?: ex.toString())
        throw ex
    } finally {
        span.end()
    }
}

inline fun <T> Tracer.withSpan(parent: Span?, spanName: String, block: (Span) -> T): T {
    val span = (parent?.let { this.nextSpan(it) } ?: this.nextSpan()).name(spanName).start()
    try {
        return block(span)
    } catch (ex: Exception) {
        span.tag("error", ex.message ?: ex.toString())
        throw ex
    } finally {
        span.end()
    }
}