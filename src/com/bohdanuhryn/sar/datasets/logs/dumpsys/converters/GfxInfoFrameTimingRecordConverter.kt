package com.bohdanuhryn.sar.datasets.logs.dumpsys.converters

import com.bohdanuhryn.sar.core.parsers.MultiLineRecordConverter
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GfxInfoFrameTimingRecord
import java.util.regex.Pattern

class GfxInfoFrameTimingRecordConverter(
    private val containsBufferDuration: Boolean
) : MultiLineRecordConverter<GfxInfoFrameTimingRecord>() {

    private val START_LINE = "---PROFILEDATA---"
    private val END_LINE = "---PROFILEDATA---"

    private val HEADER_PREFIX_REGEX = "Graphics info for pid"
    private val ID_REGEX = "(\\d+)"
    private val PID_REGEX = ID_REGEX
    private val PACKAGE_REGEX = "\\[(.*)\\]"
    private val SEP = "\\s+"
    private val SEP_OPT = "\\s*"
    private val STARS_REGEX = "\\*\\*"

    private val fields = arrayOf(
        STARS_REGEX, SEP,
        HEADER_PREFIX_REGEX, SEP,
        PID_REGEX, SEP,
        PACKAGE_REGEX, SEP,
        STARS_REGEX
    )

    private val pattern: Pattern = Pattern.compile("^" + fields.fold("", { s, f -> "$s$f" }) + "$")

    private var endLine: Boolean = true

    override fun isStartLine(line: String): Boolean {
        return pattern.matcher(line.trim()).matches()
    }

    override fun isEndLine(line: String): Boolean {
        if (line.contains(END_LINE)) {
            endLine = !endLine
            return endLine
        }
        return false
    }

    override fun isValidLine(line: String): Boolean {
        return try {
            val cells = line.split(",").filter { it.isNotBlank() }
            val allInts = cells.all { try { it.toLong(); true } catch (e: Exception) { false } }
            cells.size == cellsCount && allInts
        } catch (e: Exception) {
            false
        }
    }

    private val cellsCount = if (containsBufferDuration) 16 else 14

    override fun convert(lines: List<String>): GfxInfoFrameTimingRecord? {
        return null
    }

    override fun convertAll(lines: List<String>): List<GfxInfoFrameTimingRecord> {
        try {
            val headerMather = pattern.matcher(lines[0].trim())
            headerMather.matches()
            val pid = headerMather.group(1).trim().toInt()
            val packageName = headerMather.group(2)
            return lines.filter { isValidLine(it) }.map { line ->
                val cells = line.split(",").filter { it.isNotBlank() }
                val ints = cells.map { try { it.toLong() } catch(e: Exception) { 0L } }.toList()
                GfxInfoFrameTimingRecord(
                    pid = pid,
                    packageName = packageName,
                    flags = ints[0],
                    intendedVsync = ints[1],
                    vsync = ints[2],
                    oldestInputEvent = ints[3],
                    newestInputEvent = ints[4],
                    handleInputStart = ints[5],
                    animationStart = ints[6],
                    performTraversalsStart = ints[7],
                    drawStart = ints[8],
                    syncQueued = ints[9],
                    syncStart = ints[10],
                    issueDrawCommandsStart = ints[11],
                    swapBuffers = ints[12],
                    frameCompleted = ints[13],
                    dequeueBufferDuration = if (containsBufferDuration) ints[14] else 0,
                    queueBufferDuration = if (containsBufferDuration) ints[15] else 0
                )
            }
        } catch (e: Exception) {
            println(e.message ?: "Something went wrong")
            return emptyList()
        }
    }

}