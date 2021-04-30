package com.bohdanuhryn.sar.core.datasets

abstract class TimeSeriesRecord(
    val timeStartSec: Long,
    val timeEndSec: Long
) {

    interface RecordMetadata<OR : TimeSeriesRecord> {

        fun hideFields(): List<String>

    }

    interface RecordBuilder<OR : TimeSeriesRecord> {

        fun build(timeStartSec: Long, timeEndSec: Long, originRecords: List<IntermediateRecord>): List<OR>

    }

}