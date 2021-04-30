package com.bohdanuhryn.sar.core.parsers

interface LogsParser<T : LogRecord> {

    fun parse(): List<T>

}