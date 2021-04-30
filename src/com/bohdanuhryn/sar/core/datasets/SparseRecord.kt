package com.bohdanuhryn.sar.core.datasets

import com.bohdanuhryn.sar.utils.csv.CsvRow

class SparseRecord(
    private val builders: List<IntermediateRecord.RecordMetaData>,
    private val record: IntermediateRecord
) : CsvRow {

    fun getTimeStamp(): Long {
        return record.timeStampMs
    }

    fun getColumnNames(): List<String> {
        return builders.map { it.getColumnNamesFull() }.flatten()
    }

    fun getRows(): List<String> {
        return builders.map {
            if (it.getTag() == record.tag) {
                record.getRow()
            } else {
                it.getColumnsCount().downTo(1).map { "" }
            }
        }.flatten()
    }

    override fun getCsvCells(): List<String> {
        return arrayListOf<String>().apply {
            add(getTimeStamp().toString())
            addAll(getRows())
        }
    }

    override fun getCsvColumns(): List<String> {
        return arrayListOf<String>().apply {
            add("timestamp")
            addAll(getColumnNames())
        }
    }
}