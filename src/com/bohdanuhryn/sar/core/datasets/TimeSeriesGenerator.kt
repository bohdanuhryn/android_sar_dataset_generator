package com.bohdanuhryn.sar.core.datasets

/**
 * IR - input record
 * OR - output record
 */
class TimeSeriesGenerator(
    private val intervalSec: Int,
    private val records: List<IntermediateRecord>
) {

    fun <OR : TimeSeriesRecord> generate(timeSlotBuilder: TimeSeriesRecord.RecordBuilder<OR>): List<OR> {
        val output = ArrayList<OR>()
        records
            .sortedBy { it.timeStampMs }
            .let { sortedRecords ->
                val minTime = sortedRecords.first().timeStampMs
                val maxTime = sortedRecords.last().timeStampMs
                val intervalsCount = maxTime.minus(minTime).div(intervalSec * 1000).toInt()
                val intervalRanges = getIntervalRanges(minTime, intervalsCount)
                sortedRecords
                    .groupBy { record ->
                        intervalRanges.find { range ->
                            record.timeStampMs >= range.first && record.timeStampMs < range.second
                        }?.first ?: 0L
                    }
                    .map { timeSlotBuilder.build(it.key / 1000, (it.key / 1000) + intervalSec, it.value) }
                    .forEach { output.addAll(it) }
            }
        return output
    }

    private fun getIntervalRanges(startTime: Long, intervalsCount: Int): List<Pair<Long, Long>> {
        return 0.rangeTo(intervalsCount)
            .map { Pair(startTime + (it * intervalSec * 1000), startTime + ((it + 1) * intervalSec * 1000)) }
            .toList()
    }

}