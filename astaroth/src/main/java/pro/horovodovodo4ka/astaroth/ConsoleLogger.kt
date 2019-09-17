package pro.horovodovodo4ka.astaroth

import android.util.Log
import android.util.Log.ASSERT
import android.util.Log.DEBUG
import android.util.Log.ERROR
import android.util.Log.INFO
import android.util.Log.VERBOSE
import android.util.Log.WARN
import pro.horovodovodo4ka.astaroth.LogLevel.Debug
import pro.horovodovodo4ka.astaroth.LogLevel.Error
import pro.horovodovodo4ka.astaroth.LogLevel.Info
import pro.horovodovodo4ka.astaroth.LogLevel.Verbose
import pro.horovodovodo4ka.astaroth.LogLevel.Warning
import pro.horovodovodo4ka.astaroth.LogLevel.WhatTheFuck
import pro.horovodovodo4ka.astaroth.Logger.Config

private fun LogLevel.systemLevel(): Int = when (this) {
    Verbose -> VERBOSE
    Debug -> DEBUG
    Info -> INFO
    Warning -> WARN
    Error -> ERROR
    WhatTheFuck -> ASSERT
}

class ConsoleLogger(override var config: Config = Config()) : Logger {
    override fun log(message: Lazy<Any>, level: LogLevel, type: LogType) {
        if (!isAbleToLog(level, type)) return

        val tag = type.logTag
        val systemLevel = level.systemLevel()

        Log.println(systemLevel, tag, message.value.toString())
    }

    override fun toString(): String = "ConsoleLogger: $config"
}