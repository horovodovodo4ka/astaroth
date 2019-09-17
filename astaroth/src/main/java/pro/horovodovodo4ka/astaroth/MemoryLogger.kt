package pro.horovodovodo4ka.astaroth

import pro.horovodovodo4ka.astaroth.Logger.Config
import java.util.Locale

typealias MemoryLoggerRecord = Triple<Any, LogLevel, LogType>

private object NoInit
class MemoryLogger(override var config: Config = Config(), private val maxRecords: Int = 50) : Logger {
    private val messages = Array<Any>(maxRecords) { NoInit }
    private var cursor: Int = 0

    override fun log(message: Lazy<Any>, level: LogLevel, type: LogType) {
        if (!isAbleToLog(level, type)) return
        messages[cursor] = MemoryLoggerRecord(message.value, level, type)
        cursor = (cursor + 1) % maxRecords
    }

    override fun toString(): String {
        return "MemoryLogger: $config"
    }

    @Suppress("UNCHECKED_CAST")
    val content: String
        get() = (0 until maxRecords)
            .map { it + cursor }
            .map { messages[it % maxRecords] }
            .filterIsInstance<MemoryLoggerRecord>()
            .joinToString("\r\n") {
                val (message, level, type) = it
                val tag = type.logTag
                "${level.name.toUpperCase(Locale.getDefault())}: [$tag] $message"
            }
}
