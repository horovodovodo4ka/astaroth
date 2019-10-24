package pro.horovodovodo4ka.astaroth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import pro.horovodovodo4ka.astaroth.LogLevel.Debug
import pro.horovodovodo4ka.astaroth.LogLevel.Error
import pro.horovodovodo4ka.astaroth.LogLevel.Info
import pro.horovodovodo4ka.astaroth.LogLevel.Verbose
import pro.horovodovodo4ka.astaroth.LogLevel.Warning
import pro.horovodovodo4ka.astaroth.LogLevel.WhatTheFuck
import pro.horovodovodo4ka.astaroth.Logger.Config
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

enum class LogLevel {
    Verbose, Debug, Info, Warning, Error, WhatTheFuck;
}

interface LogType {
    val logTag: String
        get() = this::class.simpleName!!
}

typealias LoggerRecord = Triple<Lazy<Any>, LogLevel, LogType>

interface Logger {
    data class Config(
        var minimumLevel: LogLevel = Debug,
        var allowedTypes: MutableSet<LogType> = mutableSetOf(),
        var disallowedTypes: MutableSet<LogType> = mutableSetOf(),
        var contextFormat: String? = null
    )

    fun log(message: Lazy<Any>, level: LogLevel, type: LogType)

    var config: Config
}

fun Logger.isAbleToLog(level: LogLevel, type: LogType): Boolean {
    if (level < config.minimumLevel) return false
    if (config.allowedTypes.isNotEmpty() && !config.allowedTypes.contains(type)) return false
    if (config.disallowedTypes.isNotEmpty() && config.disallowedTypes.contains(type)) return false
    return true
}

object Log : Logger {
    override var config = Config()

    private val loggers = mutableSetOf<Logger>()

    fun addLoggers(vararg loggers: Logger) {
        synchronized(this.loggers) {
            this.loggers.addAll(loggers)
        }
    }

    fun removeLoggers(vararg loggers: Logger) {
        synchronized(this.loggers) {
            this.loggers.removeAll(loggers)
        }
    }

    fun allowType(type: LogType) {
        synchronized(this.config) {
            config.allowedTypes.add(type)
        }
    }

    fun disallowType(type: LogType) {
        synchronized(this.config) {
            config.disallowedTypes.add(type)
        }
    }

    private object Scope : CoroutineScope {
        override val coroutineContext: CoroutineContext get() = EmptyCoroutineContext
    }

    // prevent multithread access
    private var logChannel = Channel<LoggerRecord>()

    init {
        Scope.launch(IO) {
            while (true) {
                val (message, level, type) = logChannel.receive()
                synchronized(loggers) {
                    loggers.forEach { it.log(message, level, type) }
                }
            }
        }
    }

    override fun log(message: Lazy<Any>, level: LogLevel, type: LogType) {
        if (!isAbleToLog(level, type)) return

        Scope.launch { logChannel.send(LoggerRecord(message, level, type)) }
    }

    internal fun log(message: Lazy<Any>, level: LogLevel, type: LogType, context: StackTraceElement?) {
        log(lazy { "${config.format(context)} ${message.value}" }, level, type)
    }
}

private fun Config.format(traceElement: StackTraceElement?): String {
    traceElement ?: return ""
    return "${traceElement.fileName}:${traceElement.lineNumber} (${traceElement.methodName})"
}

operator fun Log.plusAssign(logger: Logger) = addLoggers(logger)
operator fun Log.minusAssign(logger: Logger) = removeLoggers(logger)

fun Log.v(type: LogType, message: Any) = log(message = lazy { message }, level = Verbose, type = type, context = Throwable().stackTrace.getOrNull(1))
fun Log.d(type: LogType, message: Any) = log(message = lazy { message }, level = Debug, type = type, context = Throwable().stackTrace.getOrNull(1))
fun Log.i(type: LogType, message: Any) = log(message = lazy { message }, level = Info, type = type, context = Throwable().stackTrace.getOrNull(1))
fun Log.w(type: LogType, message: Any) = log(message = lazy { message }, level = Warning, type = type, context = Throwable().stackTrace.getOrNull(1))
fun Log.e(type: LogType, message: Any) = log(message = lazy { message }, level = Error, type = type, context = Throwable().stackTrace.getOrNull(1))
fun Log.wtf(type: LogType, message: Any) = log(message = lazy { message }, level = WhatTheFuck, type = type, context = Throwable().stackTrace.getOrNull(1))

fun Log.v(type: LogType, context: StackTraceElement? = Throwable().stackTrace.getOrNull(1), lazyMessage: () -> Any) = log(message = lazy(lazyMessage), level = Verbose, type = type, context = context)
fun Log.d(type: LogType, context: StackTraceElement? = Throwable().stackTrace.getOrNull(1), lazyMessage: () -> Any) = log(message = lazy(lazyMessage), level = Debug, type = type, context = context)
fun Log.i(type: LogType, context: StackTraceElement? = Throwable().stackTrace.getOrNull(1), lazyMessage: () -> Any) = log(message = lazy(lazyMessage), level = Info, type = type, context = context)
fun Log.w(type: LogType, context: StackTraceElement? = Throwable().stackTrace.getOrNull(1), lazyMessage: () -> Any) = log(message = lazy(lazyMessage), level = Warning, type = type, context = context)
fun Log.e(type: LogType, context: StackTraceElement? = Throwable().stackTrace.getOrNull(1), lazyMessage: () -> Any) = log(message = lazy(lazyMessage), level = Error, type = type, context = context)
fun Log.wtf(type: LogType, context: StackTraceElement? = Throwable().stackTrace.getOrNull(1), lazyMessage: () -> Any) = log(message = lazy(lazyMessage), level = WhatTheFuck, type = type, context = context)
