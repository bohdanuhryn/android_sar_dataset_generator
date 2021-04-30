package com.bohdanuhryn.sar.core.parsers

import java.io.File
import java.lang.Exception

class MultiLineLogsParser(
    private val path: String,
    private val converters: List<MultiLineRecordConverter<*>>
) {

    fun parse(): List<LogRecord> {
        val records: ArrayList<LogRecord> = ArrayList()
        var converter: MultiLineRecordConverter<*>? = null
        val linesToConvert: ArrayList<String> = ArrayList()
        File(path).bufferedReader(charset = Charsets.UTF_16).useLines { lines ->
            lines.forEach { line ->
                try {
                    when {
                        converter == null -> {
                            converter = converters.first { it.isStartLine(line) }
                            if (converter != null) {
                                linesToConvert.clear()
                                linesToConvert.add(line)
                            }
                        }
                        converter?.isEndLine(line) == true -> {
                            linesToConvert.add(line)
                            val record = converter?.convert(linesToConvert)
                            record?.let { records.add(it) }
                            records.addAll(converter?.convertAll(linesToConvert) ?: emptyList())
                            converter = null
                        }
                        converter?.isValidLine(line) == true -> {
                            linesToConvert.add(line)
                        }
                    }
                } catch (e: Exception) {
                    println(e.message ?: "Something went wrong")
                }
            }
        }
        return records
    }

}