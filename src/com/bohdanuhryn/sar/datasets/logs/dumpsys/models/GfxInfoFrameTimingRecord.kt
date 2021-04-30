package com.bohdanuhryn.sar.datasets.logs.dumpsys.models

import com.bohdanuhryn.sar.core.parsers.LogRecord
import com.bohdanuhryn.sar.utils.csv.CsvRow

data class GfxInfoFrameTimingRecord(
    val pid: Int,
    val packageName: String,
    val flags: Long,
    val intendedVsync: Long,
    val vsync: Long,
    val oldestInputEvent: Long,
    val newestInputEvent: Long,
    val handleInputStart: Long,
    val animationStart: Long,
    val performTraversalsStart: Long,
    val drawStart: Long,
    val syncQueued: Long,
    val syncStart: Long,
    val issueDrawCommandsStart: Long,
    val swapBuffers: Long,
    val frameCompleted: Long,
    val dequeueBufferDuration: Long,
    val queueBufferDuration: Long
) : LogRecord, CsvRow {

    override fun getCsvCells(): List<String> = listOf(
        pid,
        packageName,
        flags,
        intendedVsync,
        vsync,
        oldestInputEvent,
        newestInputEvent,
        handleInputStart,
        animationStart,
        performTraversalsStart,
        drawStart,
        syncQueued,
        syncStart,
        issueDrawCommandsStart,
        swapBuffers,
        frameCompleted,
        dequeueBufferDuration,
        queueBufferDuration
    ).map { it.toString() }.toList()

    override fun getCsvColumns(): List<String> = listOf(
        "pid",
        "packageName",
        "flags",
        "intendedVsync",
        "vsync",
        "oldestInputEvent",
        "newestInputEvent",
        "handleInputStart",
        "animationStart",
        "performTraversalsStart",
        "drawStart",
        "syncQueued",
        "syncStart",
        "issueDrawCommandsStart",
        "swapBuffers",
        "frameCompleted",
        "dequeueBufferDuration",
        "queueBufferDuration"
    )

}