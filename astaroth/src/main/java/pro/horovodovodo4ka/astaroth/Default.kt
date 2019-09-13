package pro.horovodovodo4ka.astaroth

import pro.horovodovodo4ka.astaroth.LogLevel.Debug
import pro.horovodovodo4ka.astaroth.LogLevel.Error
import pro.horovodovodo4ka.astaroth.LogLevel.Info
import pro.horovodovodo4ka.astaroth.LogLevel.Verbose
import pro.horovodovodo4ka.astaroth.LogLevel.Warning
import pro.horovodovodo4ka.astaroth.LogLevel.WhatTheFuck

object Default : LogType

@JvmName("log_verbose_default")
fun Log.v(message: Any) = log(message = message, level = Verbose, type = Default)

@JvmName("log_verbose_debug")
fun Log.d(message: Any) = log(message = message, level = Debug, type = Default)

@JvmName("log_verbose_info")
fun Log.i(message: Any) = log(message = message, level = Info, type = Default)

@JvmName("log_verbose_warning")
fun Log.w(message: Any) = log(message = message, level = Warning, type = Default)

@JvmName("log_verbose_error")
fun Log.e(message: Any) = log(message = message, level = Error, type = Default)

@JvmName("log_verbose_wtf")
fun Log.wtf(message: Any) = log(message = message, level = WhatTheFuck, type = Default)