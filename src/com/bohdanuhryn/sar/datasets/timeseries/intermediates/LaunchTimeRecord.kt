package com.bohdanuhryn.sar.datasets.timeseries.intermediates

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.datasets.logs.logcat.models.LCActivityRecord

class LaunchTimeRecord(
    val processId: Int = 0,
    val packageName: String = "",
    val launchTime: Long = 0,
    timeStampMs: Long = 0,
    tag: String = ""
) : IntermediateRecord(timeStampMs, tag) {

    override fun getRow(): List<String> = listOf(
        processId.toString(),
        packageName,
        launchTime.toString()
    )

    class MetaData : RecordMetaData() {

        override fun getTag(): String = LaunchTimeRecord::class.java.simpleName

        override fun getColumnsCount(): Int = 3

        override fun getColumnPrefix(): String = "activity_launch"

        override fun getColumnPostfixes(): List<String> = listOf(
            "",
            "",
            "ms"
        )

        override fun getColumnNames(): List<String> = listOf(
            "process_id",
            "package_name",
            "launch_time"
        )

    }

    class Builder : RecordBuilder<LCActivityRecord, LaunchTimeRecord> {

        private val metaData: MetaData = MetaData()

        override fun build(origin: LCActivityRecord): LaunchTimeRecord {
            val time = origin.timestamp.toFloatOrNull() ?: 0F
            val launchTime = origin.time
                .replace("+", "")
                .replace("ms", " ")
                .replace("s", " ")
                .split(" ")
                .filter { it.isNotBlank() }
                .foldRightIndexed(0L) { i: Int, v: String, acc: Long ->
                    val value = v.toIntOrNull() ?: 0
                    when (i) {
                        0 -> acc + value
                        1 -> acc + (value * 1000)
                        else -> acc
                    }
                }
            return LaunchTimeRecord(
                packageName = origin.packageName,
                processId = origin.pid,
                launchTime = launchTime,
                timeStampMs = if (time > 0F) (time * 1000).toLong() else 0,
                tag = metaData.getTag()
            )
        }

    }

}

fun List<LaunchTimeRecord>.groupByProcessId(): List<LaunchTimeRecord> {
    return this
        .groupBy { it.processId }
        .map { it.value.reduceAll() }
}

fun List<LaunchTimeRecord>.reduceAll(): LaunchTimeRecord {
    return this
        .reduce { acc, rec ->
            LaunchTimeRecord(
                timeStampMs = acc.timeStampMs + rec.timeStampMs,
                launchTime = acc.launchTime + rec.launchTime,
                tag = acc.tag,
                packageName = acc.packageName,
                processId = acc.processId
            )
        }.let { finalRecord ->
            LaunchTimeRecord(
                timeStampMs = finalRecord.timeStampMs / this.size,
                launchTime = finalRecord.launchTime / this.size,
                tag = finalRecord.tag,
                packageName = finalRecord.packageName,
                processId = finalRecord.processId
            )
        }
}
