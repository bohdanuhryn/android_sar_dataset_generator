package com.bohdanuhryn.sar.datasets.timeseries.intermediates

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GfxInfoFrameTimingRecord

class FrameTimeRecord(
    val processId: Int = 0,
    val packageName: String = "",
    val drawTimeMs: Float = 0.0F,
    timeStampMs: Long = 0,
    tag: String = ""
) : IntermediateRecord(timeStampMs, tag) {

    override fun getRow(): List<String> = listOf(
        processId.toString(),
        packageName,
        drawTimeMs.toString()
    )

    class MetaData : RecordMetaData() {

        override fun getTag(): String = FrameTimeRecord::class.java.simpleName

        override fun getColumnsCount(): Int = 3

        override fun getColumnPrefix(): String = "frame_time"

        override fun getColumnPostfixes(): List<String> = listOf(
            "",
            "",
            "ms"
        )

        override fun getColumnNames(): List<String> = listOf(
            "process_id",
            "package_name",
            "draw_time"
        )

    }

    class Builder : RecordBuilder<GfxInfoFrameTimingRecord, FrameTimeRecord> {

        private val metaData: MetaData = MetaData()

        override fun build(origin: GfxInfoFrameTimingRecord): FrameTimeRecord {
            val time = origin.intendedVsync
            val drawTime = (origin.frameCompleted - origin.intendedVsync) / 1000000.0F
            if (drawTime < 0) {
                println("Draw Time: ${origin.intendedVsync} - ${origin.frameCompleted} = $drawTime")
            }
            return FrameTimeRecord(
                processId = origin.pid,
                packageName = origin.packageName,
                drawTimeMs = (origin.frameCompleted - origin.intendedVsync) / 1000000.0F,
                timeStampMs = if (time > 0) time / 1000000 else 0,
                tag = metaData.getTag()
            )
        }

    }

}

fun List<FrameTimeRecord>.groupByProcessId(): List<FrameTimeRecord> {
    return this
        .filter { it.drawTimeMs >= 0 }
        .groupBy { it.packageName }
        .map { it.value.reduceAll() }
}

fun List<FrameTimeRecord>.reduceAll(): FrameTimeRecord {
    return this.reduce { acc, rec ->
        FrameTimeRecord(
            processId = acc.processId,
            packageName = acc.packageName,
            timeStampMs = acc.timeStampMs + rec.timeStampMs,
            drawTimeMs = acc.drawTimeMs + rec.drawTimeMs,
            tag = acc.tag
        )
    }.let { finalRecord ->
        FrameTimeRecord(
            processId = finalRecord.processId,
            packageName = finalRecord.packageName,
            timeStampMs = finalRecord.timeStampMs / this.size,
            drawTimeMs = finalRecord.drawTimeMs / this.size,
            tag = finalRecord.tag
        )
    }
}
