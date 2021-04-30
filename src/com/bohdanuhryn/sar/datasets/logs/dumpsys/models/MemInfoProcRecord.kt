package com.bohdanuhryn.sar.datasets.logs.dumpsys.models

import com.bohdanuhryn.sar.core.parsers.LogRecord
import com.bohdanuhryn.sar.utils.csv.CsvRow

data class MemInfoProcRecord(
    val time: String,
    val processGroup: String,
    val packageName: String,
    val pid: String,
    val pss: String,
    val unknownFieldWithNA: String,// TODO: get info about field
    val resultType: String,// TODO: get info about field
    val totalRam: String,
    val freeRam: String,
    val usedRam: String,
    val lostRam: String
) : LogRecord, CsvRow {

    override fun getCsvCells(): List<String> = listOf(
        time,
        processGroup,
        packageName,
        pid,
        pss,
        unknownFieldWithNA,
        resultType,
        totalRam,
        freeRam,
        usedRam,
        lostRam
    )

    override fun getCsvColumns(): List<String> = listOf(
        "time",
        "processGroup",
        "packageName",
        "pid",
        "pss",
        "unknownFieldWithNA",
        "resultType",
        "totalRam",
        "freeRam",
        "usedRam",
        "lostRam"
    )

}