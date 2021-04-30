package com.bohdanuhryn.sar.datasets.timeseries

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.core.datasets.TimeSeriesRecord
import com.bohdanuhryn.sar.datasets.timeseries.intermediates.*
import com.bohdanuhryn.sar.utils.csv.CsvRow

class UserPerceivedResponseMetricsRecord(
    private val ramUsage: RamUsageRecord,
    private val frameTime: FrameTimeRecord,
    private val launchTime: LaunchTimeRecord,
    private val frameStats: FrameStatsRecord,
    timeStart: Long,
    timeEnd: Long
) : TimeSeriesRecord(timeStart, timeEnd), CsvRow {

    override fun getCsvCells(): List<String> = arrayListOf<String>().apply {
        add(timeStartSec.toString())
        add(timeEndSec.toString())
        addAll(ramUsage.getRow())
        addAll(launchTime.getRow())
        addAll(frameTime.getRow())
        addAll(frameStats.getRow())
    }

    override fun getCsvColumns(): List<String> = arrayListOf<String>().apply {
        add("time_start_sec")
        add("time_end_sec")
        addAll(RamUsageRecord.MetaData().getColumnNames())
        addAll(LaunchTimeRecord.MetaData().getColumnNames())
        addAll(FrameTimeRecord.MetaData().getColumnNames())
        addAll(FrameStatsRecord.MetaData().getColumnNames())
    }

    class MetaData : RecordMetadata<UserPerceivedResponseMetricsRecord> {

        override fun hideFields(): List<String> = listOf(
            "package_name",
            "process_group",
            "pid"
        )

    }

    class Builder : RecordBuilder<UserPerceivedResponseMetricsRecord> {

        override fun build(
            timeStartSec: Long,
            timeEndSec: Long,
            originRecords: List<IntermediateRecord>
        ): List<UserPerceivedResponseMetricsRecord> {
            val ramUsageRecord: RamUsageRecord = originRecords.filterIsInstance(RamUsageRecord::class.java)
                .let { records ->
                    if (records.isNotEmpty()) {
                        records.reduceAll()
                    } else {
                        RamUsageRecord()
                    }
                }
            val frameTimeRecord: FrameTimeRecord = originRecords.filterIsInstance(FrameTimeRecord::class.java)
                .let { records ->
                    if (records.isNotEmpty()) {
                        records.reduceAll()
                    } else {
                        FrameTimeRecord()
                    }
                }
            val frameStatsRecord: FrameStatsRecord = originRecords.filterIsInstance(FrameStatsRecord::class.java)
                .let { records ->
                    if (records.isNotEmpty()) {
                        val filtered = ArrayList<FrameStatsRecord>()
                        records.groupBy { it.packageName }.forEach { (_, packageNameGroup) ->
                            packageNameGroup.groupBy { it.timeStampMs }.forEach { (_, timeStampGroup) ->
                                timeStampGroup.maxBy { it.totalFrames }?.let { filtered.add(it) }
                            }
                        }
                        filtered.reduceAll()
                    } else {
                        FrameStatsRecord()
                    }
                }
            val launchTimeRecord: LaunchTimeRecord = originRecords.filterIsInstance(LaunchTimeRecord::class.java)
                .let { records ->
                    if (records.isNotEmpty()) {
                        records.reduceAll()
                    } else {
                        LaunchTimeRecord()
                    }
                }
            return listOf(
                UserPerceivedResponseMetricsRecord(
                    timeStart = timeStartSec,
                    timeEnd = timeEndSec,
                    frameTime = frameTimeRecord,
                    ramUsage = ramUsageRecord,
                    launchTime = launchTimeRecord,
                    frameStats = frameStatsRecord
                )
            )
        }

    }

}