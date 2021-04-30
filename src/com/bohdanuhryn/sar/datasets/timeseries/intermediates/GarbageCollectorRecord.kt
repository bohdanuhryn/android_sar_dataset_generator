package com.bohdanuhryn.sar.datasets.timeseries.intermediates

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.datasets.logs.logcat.models.LCGarbageCollectorRecord

class GarbageCollectorRecord(
    val processId: Int = 0,
    val pausedTime: Float = 0F,
    val totalTime: Float = 0F,
    timeStampMs: Long = 0,
    tag: String = ""
) : IntermediateRecord(timeStampMs, tag) {

    override fun getRow(): List<String> = listOf(
        processId.toString(),
        pausedTime.toString(),
        totalTime.toString()
    )

    class MetaData : RecordMetaData() {

        override fun getTag(): String = GarbageCollectorRecord::class.java.simpleName

        override fun getColumnsCount(): Int = 3

        override fun getColumnPrefix(): String = "garbage_collector"

        override fun getColumnPostfixes(): List<String> = listOf(
            "",
            "ms",
            "ms"
        )

        override fun getColumnNames(): List<String> = listOf(
            "process_id",
            "paused_time",
            "total_time"
        )

    }

    class Builder : RecordBuilder<LCGarbageCollectorRecord, GarbageCollectorRecord> {

        private val metaData: MetaData = MetaData()

        override fun build(origin: LCGarbageCollectorRecord): GarbageCollectorRecord {
            val time = origin.timestamp.toFloatOrNull() ?: 0F
            val pausedTime = origin.pausedTimeMs.toFloatOrNull() ?: 0F
            val totalTime = origin.totalTimeMs.toFloatOrNull() ?: 0F
            return GarbageCollectorRecord(
                processId = origin.pid,
                pausedTime = pausedTime,
                totalTime = totalTime,
                timeStampMs = if (time > 0F) (time * 1000).toLong() else 0,
                tag = metaData.getTag()
            )
        }

    }

}

fun List<GarbageCollectorRecord>.groupByProcessId(): List<GarbageCollectorRecord> {
    return this
        .groupBy { it.processId }
        .map { it.value.reduceAll() }
}

fun List<GarbageCollectorRecord>.reduceAll(): GarbageCollectorRecord {
    return this
        .reduce { acc, rec ->
            GarbageCollectorRecord(
                timeStampMs = acc.timeStampMs + rec.timeStampMs,
                totalTime = acc.totalTime + rec.totalTime,
                pausedTime = acc.pausedTime + rec.pausedTime,
                tag = acc.tag,
                processId = acc.processId
            )
        }.let { finalRecord ->
            GarbageCollectorRecord(
                timeStampMs = finalRecord.timeStampMs / this.size,
                totalTime = finalRecord.totalTime / this.size,
                pausedTime = finalRecord.pausedTime / this.size,
                tag = finalRecord.tag,
                processId = finalRecord.processId
            )
        }
}
