package com.bohdanuhryn.sar.datasets.timeseries.intermediates

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.MemInfoProcRecord

class RamUsageRecord(
    val processId: Int = 0,
    val packageName: String = "",
    val processGroup: String = "",
    val totalUsedKb: Long = 0,
    val totalFreeKb: Long = 0,
    val totalLostKb: Long = 0,
    val totalKb: Long = 0,
    val pssKb: Long = 0,
    timeStampMs: Long = 0,
    tag: String = ""
) : IntermediateRecord(timeStampMs, tag) {

    override fun getRow(): List<String> = listOf(
        processId.toString(),
        packageName,
        processGroup,
        pssKb.toString(),
        totalUsedKb.toString(),
        totalFreeKb.toString(),
        totalLostKb.toString(),
        totalKb.toString()
    )

    class MetaData : RecordMetaData() {

        override fun getTag(): String = RamUsageRecord::class.java.simpleName

        override fun getColumnsCount(): Int = 8

        override fun getColumnPrefix(): String = "ram_usage"

        override fun getColumnPostfixes(): List<String> = listOf(
            "",
            "",
            "",
            "kb",
            "kb",
            "kb",
            "kb",
            "kb"
        )

        override fun getColumnNames(): List<String> = listOf(
            "process_id",
            "package_name",
            "process_group",
            "pss",
            "total_used",
            "total_free",
            "total_lost",
            "total"
        )

    }

    class Builder : RecordBuilder<MemInfoProcRecord, RamUsageRecord> {

        private val metaData: MetaData = MetaData()

        override fun build(origin: MemInfoProcRecord): RamUsageRecord {
            val time = origin.time.toIntOrNull() ?: 0
            return RamUsageRecord(
                processGroup = origin.processGroup,
                packageName = origin.packageName,
                processId = origin.pid.toIntOrNull() ?: 0,
                totalFreeKb = origin.freeRam.toLongOrNull() ?: 0,
                totalUsedKb = origin.usedRam.toLongOrNull() ?: 0,
                totalLostKb = origin.lostRam.toLongOrNull() ?: 0,
                totalKb = origin.totalRam.toLongOrNull() ?: 0,
                pssKb = origin.pss.toLongOrNull() ?: 0,
                timeStampMs = if (time > 0) time.toLong() else 0,
                tag = metaData.getTag()
            )
        }

    }

}

fun List<RamUsageRecord>.groupByProcessId(): List<RamUsageRecord> {
    return this
        .groupBy { it.processId }
        .map { it.value.reduceAll() }
}

fun List<RamUsageRecord>.reduceAll(): RamUsageRecord {
    return this
        .reduce { acc, rec ->
            RamUsageRecord(
                timeStampMs = acc.timeStampMs + rec.timeStampMs,
                pssKb = acc.pssKb + rec.pssKb,
                totalKb = acc.totalKb,
                totalUsedKb = acc.totalUsedKb + rec.totalUsedKb,
                totalFreeKb = acc.totalFreeKb + rec.totalFreeKb,
                totalLostKb = acc.totalLostKb + rec.totalLostKb,
                packageName = acc.packageName,
                processId = acc.processId,
                processGroup = acc.processGroup,
                tag = acc.tag
            )
        }.let { finalRecord ->
            RamUsageRecord(
                timeStampMs = finalRecord.timeStampMs / this.size,
                pssKb = finalRecord.pssKb / this.size,
                totalKb = finalRecord.totalKb,
                totalUsedKb = finalRecord.totalUsedKb / this.size,
                totalFreeKb = finalRecord.totalFreeKb / this.size,
                totalLostKb = finalRecord.totalLostKb / this.size,
                packageName = finalRecord.packageName,
                processId = finalRecord.processId,
                processGroup = finalRecord.processGroup,
                tag = finalRecord.tag
            )
        }
}
