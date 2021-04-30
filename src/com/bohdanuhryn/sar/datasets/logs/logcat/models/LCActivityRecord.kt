package com.bohdanuhryn.sar.datasets.logs.logcat.models

import com.bohdanuhryn.sar.core.parsers.LogRecord
import com.bohdanuhryn.sar.utils.csv.CsvRow

data class LCActivityRecord(
    val timestamp: String,
    val priority: String,
    val tag: String,
    val pid: Int,
    val tid: Int,
    val packageName: String,
    val time: String,
    val totalTime: String
) : LogRecord, CsvRow {

    override fun getCsvColumns(): List<String> = listOf("timestamp", "priority", "tag", "pid", "tid", "packageName", "time", "totalTime")

    override fun getCsvCells(): List<String> = listOf(timestamp, priority, tag, pid.toString(), tid.toString(), packageName, time, totalTime)

    override fun toString(): String {
        return "LogCatActivityRecord(" +
                "timestamp='$timestamp', " +
                "priority='$priority', tag='$tag', " +
                "pid=$pid, tid=$tid, " +
                "packageName='$packageName', " +
                "time='$time', totalTime='$totalTime')"
    }

}