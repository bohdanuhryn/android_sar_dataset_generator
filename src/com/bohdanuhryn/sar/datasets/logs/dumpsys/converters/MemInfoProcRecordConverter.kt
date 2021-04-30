package com.bohdanuhryn.sar.datasets.logs.dumpsys.converters

import com.bohdanuhryn.sar.core.parsers.MultiLineRecordConverter
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.MemInfoProcRecord
import java.lang.Exception

class MemInfoProcRecordConverter : MultiLineRecordConverter<MemInfoProcRecord>() {

    companion object {

        private const val TIME_LINE = "time"
        private const val PROC_LINE = "proc"
        private const val RAM_LINE = "ram"
        private const val LOST_RAM_LINE = "lostram"
        private const val TUNING_LINE = "tuning"

        private val LINES = listOf(
            TIME_LINE,
            PROC_LINE,
            RAM_LINE,
            LOST_RAM_LINE,
            TUNING_LINE
        )

    }

    override fun isStartLine(line: String): Boolean {
        return line.startsWith(TIME_LINE)
    }

    override fun isEndLine(line: String): Boolean {
        return line.startsWith(TUNING_LINE)
    }

    override fun isValidLine(line: String): Boolean {
        return LINES.any { line.trim().startsWith(it) }
    }

    override fun convert(lines: List<String>): MemInfoProcRecord? {
        return null
    }

    override fun convertAll(lines: List<String>): List<MemInfoProcRecord> = try {
        val time = lines.find { it.startsWith(TIME_LINE) }?.split(",")?.map { it.trim() }?.get(1) ?: "-1"
        val ram = lines.find { it.startsWith(RAM_LINE) }?.split(",")?.map { it.trim() }
        val lostRam = lines.find { it.startsWith(LOST_RAM_LINE) }?.split(",")?.map { it.trim() }
        val records: ArrayList<MemInfoProcRecord> = ArrayList()
        lines.forEach { line ->
            if (line.startsWith(PROC_LINE)) {
                val recordData = line.split(",").map { it.trim() }
                val record = MemInfoProcRecord(
                    processGroup = recordData[1],
                    packageName = recordData[2],
                    pid = recordData[3],
                    pss = recordData[4],
                    unknownFieldWithNA = recordData[5],
                    resultType = recordData[6],
                    time = time,
                    totalRam = ram?.get(1) ?: "-1",
                    freeRam = ram?.get(2) ?: "-1",
                    usedRam = ram?.get(3) ?: "-1",
                    lostRam = lostRam?.get(1) ?: "-1"
                )
                records.add(record)
            }
        }
        records
    } catch (e: Exception) {
        emptyList()
    }

}