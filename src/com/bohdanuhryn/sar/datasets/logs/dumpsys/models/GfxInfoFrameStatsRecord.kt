package com.bohdanuhryn.sar.datasets.logs.dumpsys.models

import com.bohdanuhryn.sar.core.parsers.LogRecord
import com.bohdanuhryn.sar.utils.csv.CsvRow

data class GfxInfoFrameStatsRecord(
    val packageName: String,
    val pid: String,
    val statsSince: String,
    val totalFramesRendered: String,
    val jankyFrames: String,
    val percentile50th: String,
    val percentile90th: String,
    val percentile95th: String,
    val percentile99th: String,
    val numberMissedVsync: String,
    val numberHighInputLatency: String,
    val numberSlowUiThread: String,
    val numberSlowBitmapUploads: String,
    val numberSlowIssueDrawCommands: String,
    val histogram: String
) : LogRecord, CsvRow {

    override fun getCsvCells(): List<String> = listOf(
        packageName,
        pid,
        statsSince,
        totalFramesRendered,
        jankyFrames,
        percentile50th,
        percentile90th,
        percentile95th,
        percentile99th,
        numberMissedVsync,
        numberHighInputLatency,
        numberSlowUiThread,
        numberSlowBitmapUploads,
        numberSlowIssueDrawCommands,
        histogram
    ).map { it.toString() }.toList()

    override fun getCsvColumns(): List<String> = listOf(
        "packageName",
        "pid",
        "statsSince",
        "totalFramesRendered",
        "jankyFrames",
        "percentile50th",
        "percentile90th",
        "percentile95th",
        "percentile99th",
        "numberMissedVsync",
        "numberHighInputLatency",
        "numberSlowUiThread",
        "numberSlowBitmapUploads",
        "numberSlowIssueDrawCommands",
        "histogram"
    )

}