package com.bohdanuhryn.sar.datasets.logs.logcat.models

import com.bohdanuhryn.sar.core.parsers.LogRecord
import com.bohdanuhryn.sar.utils.csv.CsvRow

data class LCGarbageCollectorRecord(
    val timestamp: String,
    val priority: String,
    val tag: String,
    val pid: Int,
    val tid: Int,
    val pausedTimeMs: String,
    val totalTimeMs: String
) : LogRecord, CsvRow {

    override fun getCsvColumns(): List<String> = listOf("timestamp", "priority", "tag", "pid", "tid", "pausedTimeMs", "totalTimeMs")

    override fun getCsvCells(): List<String> = listOf(timestamp, priority, tag, pid.toString(), tid.toString(), pausedTimeMs, totalTimeMs)

    override fun toString(): String {
        return "LCGarbageCollectorRecord(" +
                "timestamp='$timestamp', " +
                "priority='$priority', tag='$tag', " +
                "pid=$pid, tid=$tid, " +
                "pausedTimeMs='$pausedTimeMs', " +
                "totalTimeMs='$totalTimeMs')"
    }

}