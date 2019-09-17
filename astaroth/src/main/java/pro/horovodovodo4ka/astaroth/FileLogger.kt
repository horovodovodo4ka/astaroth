package pro.horovodovodo4ka.astaroth

import pro.horovodovodo4ka.astaroth.LogLevel.Debug
import pro.horovodovodo4ka.astaroth.LogLevel.Error
import pro.horovodovodo4ka.astaroth.LogLevel.Info
import pro.horovodovodo4ka.astaroth.LogLevel.Verbose
import pro.horovodovodo4ka.astaroth.LogLevel.Warning
import pro.horovodovodo4ka.astaroth.LogLevel.WhatTheFuck
import pro.horovodovodo4ka.astaroth.Logger.Config
import java.io.File
import java.io.FileOutputStream

class FileLogger(private val filename: String, override var config: Config = Config()) : Logger {

    companion object {
        private val levels = mapOf(
            Verbose to "251m",
            Debug to "35m",
            Info to "38m",
            Warning to "178m",
            Error to "197m",
            WhatTheFuck to "197m"
        )
    }

    private val file = FileOutputStream(File(filename))

    override fun log(message: Lazy<Any>, level: LogLevel, type: LogType) {
        if (!isAbleToLog(level, type)) return
        val tag = type.logTag
        val prefix = levels.getValue(level)
        val stringMessage = "\\u{001b}[38;5;$prefix[$tag] ${message.value}\\u{001b}[0m\r\n"
        file.write(stringMessage.toByteArray())
    }

    fun finalize() {
        file.close()
    }

    override fun toString(): String = "FileLogger (\"$filename\"): $config"
}