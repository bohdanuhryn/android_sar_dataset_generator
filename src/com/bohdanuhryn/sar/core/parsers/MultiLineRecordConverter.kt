package com.bohdanuhryn.sar.core.parsers

abstract class MultiLineRecordConverter<T : LogRecord> {

    abstract fun isStartLine(line: String): Boolean

    abstract fun isEndLine(line: String): Boolean

    abstract fun isValidLine(line: String): Boolean

    abstract fun convert(lines: List<String>): T?

    abstract fun convertAll(lines: List<String>): List<T>

}