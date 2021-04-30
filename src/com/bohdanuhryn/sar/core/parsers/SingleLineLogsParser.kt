package com.bohdanuhryn.sar.core.parsers

import java.io.File
import java.lang.Exception

class SingleLineLogsParser(
    private val path: String,
    private val converterBuilders: List<SingleLineRecordConverterBuilder<*>>
) {

    fun parse(): List<LogRecord> {
        val records: ArrayList<LogRecord> = ArrayList()
        try {
            File(path).bufferedReader(charset = Charsets.UTF_16).useLines { lines ->
                lines.forEach { line ->
                    try {
                        val converters = converterBuilders.map { it(line.trim()) }.toList()
                        val validConverter: SingleLineRecordConverter<*> = converters.first { it.isValid() }
                        val record = validConverter.convert()
                        record?.let { records.add(it) }
                    } catch (e: Exception) {
                        println(e.message ?: "Something went wrong")
                    }
                }
            }
        } catch (e: Exception) {
            println(e.message ?: "Something went wrong")
        }
        return records
    }

}