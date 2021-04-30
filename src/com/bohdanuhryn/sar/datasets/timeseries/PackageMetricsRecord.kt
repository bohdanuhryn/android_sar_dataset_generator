package com.bohdanuhryn.sar.datasets.timeseries

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.core.datasets.TimeSeriesRecord
import com.bohdanuhryn.sar.datasets.timeseries.intermediates.*
import com.bohdanuhryn.sar.utils.csv.CsvRow

class PackageMetricsRecord(
    private val processId: Int,
    private val packageName: String,
    private val ramUsage: RamUsageRecord,
    private val frameTime: FrameTimeRecord,
    private val launchTime: LaunchTimeRecord,
    private val garbageCollector: GarbageCollectorRecord,
    private val frameStats: FrameStatsRecord,
    timeStart: Long,
    timeEnd: Long
) : TimeSeriesRecord(timeStart, timeEnd), CsvRow {

    override fun getCsvCells(): List<String> = arrayListOf<String>().apply {
        add(timeStartSec.toString())
        add(processId.toString())
        add(packageName)
        addAll(ramUsage.getRow())
        addAll(garbageCollector.getRow())
        addAll(launchTime.getRow())
        addAll(frameTime.getRow())
        addAll(frameStats.getRow())
    }

    override fun getCsvColumns(): List<String> = arrayListOf<String>().apply {
        add("time_sec")
        add("pid")
        add("package")
        addAll(RamUsageRecord.MetaData().getColumnNames())
        addAll(GarbageCollectorRecord.MetaData().getColumnNames())
        addAll(LaunchTimeRecord.MetaData().getColumnNames())
        addAll(FrameTimeRecord.MetaData().getColumnNames())
        addAll(FrameStatsRecord.MetaData().getColumnNames())
    }

    class MetaData : RecordMetadata<PackageMetricsRecord> {

        override fun hideFields(): List<String> = listOf(
            "package_name",
            "process_id",
            ""
        )

    }

    class Builder : RecordBuilder<PackageMetricsRecord> {

        override fun build(
            timeStartSec: Long,
            timeEndSec: Long,
            originRecords: List<IntermediateRecord>
        ): List<PackageMetricsRecord> {
            val ramUsageRecordsGroupedByPackage: List<RamUsageRecord> = originRecords
                .filterIsInstance(RamUsageRecord::class.java)
                .groupByProcessId()
            val ramUsageRecord: RamUsageRecord = originRecords
                .filterIsInstance(RamUsageRecord::class.java)
                .let { if (it.isNotEmpty()) it.reduceAll() else RamUsageRecord() }

            val frameTimeRecordsGroupedByPackage: List<FrameTimeRecord> = originRecords
                .filterIsInstance(FrameTimeRecord::class.java)
                .filter { it.drawTimeMs >= 0 }
                .groupByProcessId()
            val frameTimeRecord: FrameTimeRecord = originRecords
                .filterIsInstance(FrameTimeRecord::class.java)
                .filter { it.drawTimeMs >= 0 }
                .let { if (it.isNotEmpty()) it.reduceAll() else FrameTimeRecord() }

            val frameStatsRecordsGroupedByPackage: List<FrameStatsRecord> = buildFrameStatsRecords(originRecords)
            val frameStatsRecord: FrameStatsRecord = originRecords
                .filterIsInstance(FrameStatsRecord::class.java)
                .let { if (it.isNotEmpty()) it.reduceAll() else FrameStatsRecord() }

            val launchTimeRecordsGroupedByPackage: List<LaunchTimeRecord> = originRecords
                .filterIsInstance(LaunchTimeRecord::class.java)
                .groupByProcessId()
            val launchTimeRecord: LaunchTimeRecord = originRecords
                .filterIsInstance(LaunchTimeRecord::class.java)
                .let { if (it.isNotEmpty()) it.reduceAll() else LaunchTimeRecord() }

            val garbageCollectorRecordsGroupedByPackage: List<GarbageCollectorRecord> = originRecords
                .filterIsInstance(GarbageCollectorRecord::class.java)
                .groupByProcessId()
            val garbageCollectorRecord: GarbageCollectorRecord = originRecords
                .filterIsInstance(GarbageCollectorRecord::class.java)
                .let { if (it.isNotEmpty()) it.reduceAll() else GarbageCollectorRecord() }

            val processIds: ArrayList<Int> = arrayListOf()
            processIds.addAll(ramUsageRecordsGroupedByPackage.filter {
                processIds.all { alreadyFinal -> alreadyFinal != it.processId }
            }.map { it.processId }.toList())
            processIds.addAll(frameTimeRecordsGroupedByPackage.filter {
                processIds.all { alreadyFinal -> alreadyFinal != it.processId }
            }.map { it.processId }.toList())
            processIds.addAll(frameStatsRecordsGroupedByPackage.filter {
                processIds.all { alreadyFinal -> alreadyFinal != it.processId }
            }.map { it.processId }.toList())
            processIds.addAll(launchTimeRecordsGroupedByPackage.filter {
                processIds.all { alreadyFinal -> alreadyFinal != it.processId }
            }.map { it.processId }.toList())
            processIds.addAll(garbageCollectorRecordsGroupedByPackage.filter {
                processIds.all { alreadyFinal -> alreadyFinal != it.processId }
            }.map { it.processId }.toList())

            return processIds.map { pid ->
                PackageMetricsRecord(
                    timeStart = timeStartSec,
                    timeEnd = timeEndSec,
                    frameTime = frameTimeRecordsGroupedByPackage
                        .firstOrNull { it.processId == pid } ?: frameTimeRecord,//FrameTimeRecord(),//frameTimeRecord,
                    ramUsage = ramUsageRecordsGroupedByPackage
                        .firstOrNull { it.processId == pid } ?: ramUsageRecord,//RamUsageRecord(),//ramUsageRecord,
                    garbageCollector = garbageCollectorRecordsGroupedByPackage
                        .firstOrNull { it.processId == pid } ?: GarbageCollectorRecord(),//garbageCollectorRecord,
                    launchTime = launchTimeRecordsGroupedByPackage
                        .firstOrNull { it.processId == pid } ?: launchTimeRecord,//LaunchTimeRecord(),//launchTimeRecord,
                    frameStats = frameStatsRecordsGroupedByPackage
                        .firstOrNull { it.processId == pid } ?: frameStatsRecord,//FrameStatsRecord(),//frameStatsRecord,
                    processId = pid,
                    packageName = ramUsageRecordsGroupedByPackage
                        .firstOrNull { it.processId == pid }?.packageName ?: ""
                )
            }.toList()
        }

        private fun buildFrameStatsRecords(originRecords: List<IntermediateRecord>): List<FrameStatsRecord> {
            return originRecords.filterIsInstance(FrameStatsRecord::class.java)
                .let { records ->
                    if (records.isNotEmpty()) {
                        val filtered = ArrayList<FrameStatsRecord>()
                        records.groupBy { it.processId }.forEach { (_, packageNameGroup) ->
                            packageNameGroup.groupBy { it.timeStampMs }.forEach { (_, timeStampGroup) ->
                                timeStampGroup.maxBy { it.totalFrames }?.let { filtered.add(it) }
                            }
                        }
                        filtered.groupByProcessId()
                    } else {
                        emptyList()
                    }
                }
        }

    }

}