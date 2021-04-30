package com.bohdanuhryn.sar.utils.csv

import java.io.File

class CsvWriter<T : CsvRow>(
    private val path: String,
    private val records: List<T>
) {

    fun write(hideFields: List<String> = ArrayList()) {
        if (records.isNotEmpty()) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val writer = file.writer()
            val usedFieldsIndexes: ArrayList<Int> = ArrayList()
            records.first().getCsvColumns().forEachIndexed { index, s ->
                if (!hideFields.contains(s)) usedFieldsIndexes.add(index)
            }
            val headerLine = records.first().getCsvColumns()
                .filterIndexed { index, _ ->  usedFieldsIndexes.contains(index) }
                .joinWithCommas()
            writer.appendln(headerLine)
            records.forEach { record ->
                val dataLine = record.getCsvCells()
                    .filterIndexed { index, _ ->  usedFieldsIndexes.contains(index) }
                    .joinWithCommas()
                writer.appendln(dataLine)
            }
            writer.close()
        }
    }

    private fun List<String>.joinWithCommas(): String {
        return this.fold("", {r, c -> if (r.isBlank()) c else "$r,$c" })
    }

}