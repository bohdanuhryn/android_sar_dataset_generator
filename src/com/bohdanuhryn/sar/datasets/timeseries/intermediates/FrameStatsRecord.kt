package com.bohdanuhryn.sar.datasets.timeseries.intermediates

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GfxInfoFrameStatsRecord
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GraphicsStatsRecord

class FrameStatsRecord(
    val processId: Int = 0,
    val packageName: String = "",
    val totalFrames: Long = 0,
    val jankyFrames: Long = 0,
    val percentile50thMs: Long = 0,
    val percentile90thMs: Long = 0,
    val percentile95thMs: Long = 0,
    val percentile99thMs: Long = 0,
    val numberMissedVsync: Long = 0,
    val numberHighInputLatency: Long = 0,
    val numberSlowUiThread: Long = 0,
    val numberSlowBitmapUploads: Long = 0,
    val numberSlowIssueDrawCommands: Long = 0,
    timeStampMs: Long = 0,
    tag: String = ""
) : IntermediateRecord(timeStampMs, tag) {

    override fun getRow(): List<String> = listOf(
        processId.toString(),
        packageName,
        totalFrames.toString(),
        jankyFrames.toString(),
        percentile50thMs.toString(),
        percentile90thMs.toString(),
        percentile95thMs.toString(),
        percentile99thMs.toString(),
        numberMissedVsync.toString(),
        numberHighInputLatency.toString(),
        numberSlowUiThread.toString(),
        numberSlowBitmapUploads.toString(),
        numberSlowIssueDrawCommands.toString()
    )

    class MetaData : RecordMetaData() {

        override fun getTag(): String = FrameStatsRecord::class.java.simpleName

        override fun getColumnsCount(): Int = 13

        override fun getColumnPrefix(): String = "frame_stats"

        override fun getColumnPostfixes(): List<String> = listOf(
            "",
            "",
            "",
            "",
            "ms",
            "ms",
            "ms",
            "ms",
            "",
            "",
            "",
            "",
            ""
        )

        override fun getColumnNames(): List<String> = listOf(
            "process_id",
            "package_name",
            "total_frames",
            "janky_frames",
            "percentile_50_ms",
            "percentile_90_ms",
            "percentile_95_ms",
            "percentile_99_ms",
            "missed_vsync_count",
            "high_input_latency_count",
            "slow_ui_thread_count",
            "slow_bitmap_uploads_count",
            "slow_issue_draw_commands_count"
        )

    }

    class GraphicsStatsBuilder : RecordBuilder<GraphicsStatsRecord, FrameStatsRecord> {

        private val metaData: MetaData = MetaData()

        override fun build(origin: GraphicsStatsRecord): FrameStatsRecord {
            val time = origin.statsSince.substring(0, origin.statsSince.length - 2).toLong()
            return FrameStatsRecord(
                packageName = origin.packageName.trim(),
                processId = origin.pid.trim().toInt(),
                totalFrames = origin.totalFramesRendered.trim().toLong(),
                jankyFrames = origin.jankyFrames.trim().substring(0, origin.jankyFrames.indexOfFirst { it == '(' }).trim().toLong(),
                percentile50thMs = origin.percentile50th.trim().substring(0, origin.percentile50th.length - 2).toLong(),
                percentile90thMs = origin.percentile90th.trim().substring(0, origin.percentile90th.length - 2).toLong(),
                percentile95thMs = origin.percentile95th.trim().substring(0, origin.percentile95th.length - 2).toLong(),
                percentile99thMs = origin.percentile99th.trim().substring(0, origin.percentile99th.length - 2).toLong(),
                numberMissedVsync = origin.numberMissedVsync.trim().toLong(),
                numberHighInputLatency = origin.numberHighInputLatency.trim().toLong(),
                numberSlowBitmapUploads = origin.numberSlowBitmapUploads.trim().toLong(),
                numberSlowIssueDrawCommands = origin.numberSlowIssueDrawCommands.trim().toLong(),
                numberSlowUiThread = origin.numberSlowUiThread.trim().toLong(),
                timeStampMs = if (time > 0) time / 1000000 else 0,
                tag = metaData.getTag()
            )
        }

    }

    class GfxInfoBuilder : RecordBuilder<GfxInfoFrameStatsRecord, FrameStatsRecord> {

        private val metaData: MetaData = MetaData()

        override fun build(origin: GfxInfoFrameStatsRecord): FrameStatsRecord {
            val time = origin.statsSince.substring(0, origin.statsSince.length - 2).toLong()
            return FrameStatsRecord(
                packageName = origin.packageName.trim(),
                processId = origin.pid.trim().toInt(),
                totalFrames = origin.totalFramesRendered.trim().toLong(),
                jankyFrames = origin.jankyFrames.trim().substring(0, origin.jankyFrames.indexOfFirst { it == '(' }).trim().toLong(),
                percentile50thMs = origin.percentile50th.trim().substring(0, origin.percentile50th.length - 2).toLong(),
                percentile90thMs = origin.percentile90th.trim().substring(0, origin.percentile90th.length - 2).toLong(),
                percentile95thMs = origin.percentile95th.trim().substring(0, origin.percentile95th.length - 2).toLong(),
                percentile99thMs = origin.percentile99th.trim().substring(0, origin.percentile99th.length - 2).toLong(),
                numberMissedVsync = origin.numberMissedVsync.trim().toLong(),
                numberHighInputLatency = origin.numberHighInputLatency.trim().toLong(),
                numberSlowBitmapUploads = origin.numberSlowBitmapUploads.trim().toLong(),
                numberSlowIssueDrawCommands = origin.numberSlowIssueDrawCommands.trim().toLong(),
                numberSlowUiThread = origin.numberSlowUiThread.trim().toLong(),
                timeStampMs = if (time > 0) time / 1000000 else 0,
                tag = metaData.getTag()
            )
        }

    }

}

fun List<FrameStatsRecord>.groupByProcessId(): List<FrameStatsRecord> {
    return this
        .groupBy { it.processId }
        .map { it.value.reduceAll() }
}

fun List<FrameStatsRecord>.reduceAll(): FrameStatsRecord {
    return this
        .reduce { acc, rec ->
        FrameStatsRecord(
            packageName = acc.packageName,
            processId = acc.processId,
            timeStampMs = acc.timeStampMs + rec.timeStampMs,
            totalFrames = acc.totalFrames + rec.totalFrames,
            jankyFrames = acc.jankyFrames + rec.jankyFrames,
            percentile50thMs = acc.percentile50thMs + rec.percentile50thMs,
            percentile90thMs = acc.percentile90thMs + rec.percentile90thMs,
            percentile95thMs = acc.percentile95thMs + rec.percentile95thMs,
            percentile99thMs = acc.percentile99thMs + rec.percentile99thMs,
            numberMissedVsync = acc.numberMissedVsync + rec.numberMissedVsync,
            numberSlowUiThread = acc.numberSlowUiThread + rec.numberSlowUiThread,
            numberSlowIssueDrawCommands = acc.numberSlowIssueDrawCommands + rec.numberSlowIssueDrawCommands,
            numberSlowBitmapUploads = acc.numberSlowBitmapUploads + rec.numberSlowBitmapUploads,
            numberHighInputLatency = acc.numberHighInputLatency + rec.numberHighInputLatency,
            tag = acc.tag
        )
    }.let { finalRecord ->
        FrameStatsRecord(
            packageName = finalRecord.packageName,
            processId = finalRecord.processId,
            timeStampMs = finalRecord.timeStampMs / this.size,
            totalFrames = finalRecord.totalFrames / this.size,
            jankyFrames = finalRecord.jankyFrames / this.size,
            percentile50thMs = finalRecord.percentile50thMs / this.size,
            percentile90thMs = finalRecord.percentile90thMs / this.size,
            percentile95thMs = finalRecord.percentile95thMs / this.size,
            percentile99thMs = finalRecord.percentile99thMs / this.size,
            numberHighInputLatency = finalRecord.numberHighInputLatency / this.size,
            numberSlowBitmapUploads = finalRecord.numberSlowBitmapUploads / this.size,
            numberSlowIssueDrawCommands = finalRecord.numberSlowIssueDrawCommands / this.size,
            numberSlowUiThread = finalRecord.numberSlowUiThread / this.size,
            numberMissedVsync = finalRecord.numberMissedVsync / this.size,
            tag = finalRecord.tag
        )
    }
}
