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

typealias LoggerRecord = Triple<Any, LogLevel, LogType>

interface Logger {
    data class Config(
        var minimumLevel: LogLevel = Debug,
        var allowedTypes: MutableSet<LogType> = mutableSetOf(),
        var disallowedTypes: MutableSet<LogType> = mutableSetOf(),
        var contextFormat: String? = null
    )

    fun log(message: Any, level: LogLevel, type: LogType)

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
        Log.loggers.addAll(loggers)
    }

    fun removeLoggers(vararg loggers: Logger) {
        Log.loggers.removeAll(loggers)
    }

    fun allowType(type: LogType) {
        config.allowedTypes.add(type)
    }

    fun disallowType(type: LogType) {
        config.disallowedTypes.add(type)
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
                loggers.forEach { it.log(message, level, type) }
            }
        }
    }

    override fun log(message: Any, level: LogLevel, type: LogType) {
        if (!isAbleToLog(level, type)) return

        Scope.launch { logChannel.send(Triple(message, level, type)) }
    }

    internal fun log(message: Any, level: LogLevel, type: LogType, context: StackTraceElement?) {
        log("${config.format(context)} $message", level, type)
    }
}

private fun Config.format(traceElement: StackTraceElement?): String {
    traceElement ?: return ""
    return "${traceElement.className}.${traceElement.methodName}:${traceElement.lineNumber}"
}

operator fun Log.plusAssign(logger: Logger) = addLoggers(logger)
operator fun Log.minusAssign(logger: Logger) = removeLoggers(logger)

fun Log.v(type: LogType, message: Any) = log(message = message, level = Verbose, type = type, context = Throwable().stackTrace.firstOrNull())
fun Log.d(type: LogType, message: Any) = log(message = message, level = Debug, type = type, context = Throwable().stackTrace.firstOrNull())
fun Log.i(type: LogType, message: Any) = log(message = message, level = Info, type = type, context = Throwable().stackTrace.firstOrNull())
fun Log.w(type: LogType, message: Any) = log(message = message, level = Warning, type = type, context = Throwable().stackTrace.firstOrNull())
fun Log.e(type: LogType, message: Any) = log(message = message, level = Error, type = type, context = Throwable().stackTrace.firstOrNull())
fun Log.wtf(type: LogType, message: Any) = log(message = message, level = WhatTheFuck, type = type, context = Throwable().stackTrace.firstOrNull())
