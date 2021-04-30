package com.bohdanuhryn.sar.datasets.logs.proc.converters

import com.bohdanuhryn.sar.core.parsers.SingleLineRecordConverter
import com.bohdanuhryn.sar.datasets.logs.proc.models.ProcTaskRecord

class ProcTaskRecordConverter(
    private val text: String
) : SingleLineRecordConverter<ProcTaskRecord>() {

    private val cellsCount = 52

    override fun isValid(): Boolean {
        return try {
            val cells = text.split(" ").filter { it.isNotBlank() }
            cells.size == cellsCount
        } catch (e: Exception) {
            false
        }
    }

    override fun convert(): ProcTaskRecord? {
        return try {
            val cells = text.split(" ").filter { it.isNotBlank() }
            ProcTaskRecord(
                pid = cells[0],
                threadName = cells[1].substring(1, cells[1].length - 1),
                state = cells[2],
                minorFaults = cells[9],
                minorFaultsChildren = cells[10],
                majorFaults = cells[11],
                majorFaultsChildren = cells[12],
                userTime = cells[13],
                systemTime = cells[14],
                userTimeChildren = cells[15],
                systemTimeChildren = cells[16],
                threadsNum = cells[19],
                startTime = cells[21],
                virtualMemorySize = cells[22],
                rss = cells[23],
                rssLim = cells[24]
            )
        } catch (e: Exception) {
            println(e.message ?: "Something went wrong")
            return null
        }
    }

}
