package com.bohdanuhryn.sar.datasets.logs.dumpsys.converters

import com.bohdanuhryn.sar.core.parsers.MultiLineRecordConverter
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GfxInfoFrameStatsRecord
import java.util.regex.Pattern

class GfxInfoFrameStatsRecordConverter : MultiLineRecordConverter<GfxInfoFrameStatsRecord>() {

    companion object {

        private val PACKAGE_LINE = "Package"
        private val PID_LINE = "Pid"
        private val STATS_SINCE_LINE = "Stats since"
        private val TOTAL_FRAMES_RENDERED_LINE = "Total frames rendered"
        private val JANKY_FRAMES_LINE = "Janky frames"
        private val PERCENTILE_50TH_LINE = "50th percentile"
        private val PERCENTILE_90TH_LINE = "90th percentile"
        private val PERCENTILE_95TH_LINE = "95th percentile"
        private val PERCENTILE_99TH_LINE = "99th percentile"
        private val NUMBER_MISSED_VSYNC_LINE = "Number Missed Vsync"
        private val NUMBER_HIGH_INPUT_LATENCY_LINE = "Number High input latency"
        private val NUMBER_SLOW_UI_THREAD_LINE = "Number Slow UI thread"
        private val NUMBER_SLOW_BITMAP_UPLOADS_LINE = "Number Slow bitmap uploads"
        private val NUMBER_SLOW_ISSUE_DRAW_COMMANDS_LINE = "Number Slow issue draw commands"
        private val HISTOGRAM_LINE = "HISTOGRAM"

        private val LINES = listOf(
            PACKAGE_LINE,
            PID_LINE,
            STATS_SINCE_LINE,
            TOTAL_FRAMES_RENDERED_LINE,
            JANKY_FRAMES_LINE,
            PERCENTILE_50TH_LINE,
            PERCENTILE_90TH_LINE,
            PERCENTILE_95TH_LINE,
            PERCENTILE_99TH_LINE,
            NUMBER_MISSED_VSYNC_LINE,
            NUMBER_HIGH_INPUT_LATENCY_LINE,
            NUMBER_SLOW_UI_THREAD_LINE,
            NUMBER_SLOW_BITMAP_UPLOADS_LINE,
            NUMBER_SLOW_ISSUE_DRAW_COMMANDS_LINE,
            HISTOGRAM_LINE
        )

    }

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

    override fun isStartLine(line: String): Boolean {
        return pattern.matcher(line.trim()).matches()
    }

    override fun isEndLine(line: String): Boolean {
        return line.contains("$HISTOGRAM_LINE:")
    }

    override fun isValidLine(line: String): Boolean {
        return LINES.any { line.trim().startsWith(it) }
    }

    override fun convert(lines: List<String>): GfxInfoFrameStatsRecord? {
        try {
            val map = HashMap<String, String>()
            val headerMather = pattern.matcher(lines[0].trim())
            headerMather.matches()
            map[PID_LINE] = headerMather.group(1)
            map[PACKAGE_LINE] = headerMather.group(2)
            lines.subList(1, lines.size).forEach { line ->
                LINES.find { line.startsWith(it) }?.let { key ->
                    map[key] = line.substring(key.length + 1).trim()
                }
            }
            return GfxInfoFrameStatsRecord(
                packageName = map[PACKAGE_LINE] ?: "",
                pid = map[PID_LINE] ?: "",
                histogram = map[HISTOGRAM_LINE] ?: "",
                jankyFrames = map[JANKY_FRAMES_LINE] ?: "",
                numberHighInputLatency = map[NUMBER_HIGH_INPUT_LATENCY_LINE] ?: "",
                numberMissedVsync = map[NUMBER_MISSED_VSYNC_LINE] ?: "",
                numberSlowBitmapUploads = map[NUMBER_SLOW_BITMAP_UPLOADS_LINE] ?: "",
                numberSlowIssueDrawCommands = map[NUMBER_SLOW_ISSUE_DRAW_COMMANDS_LINE] ?: "",
                numberSlowUiThread = map[NUMBER_SLOW_UI_THREAD_LINE] ?: "",
                percentile50th = map[PERCENTILE_50TH_LINE] ?: "",
                percentile90th = map[PERCENTILE_90TH_LINE] ?: "",
                percentile95th = map[PERCENTILE_95TH_LINE] ?: "",
                percentile99th = map[PERCENTILE_99TH_LINE] ?: "",
                statsSince = map[STATS_SINCE_LINE] ?: "",
                totalFramesRendered = map[TOTAL_FRAMES_RENDERED_LINE] ?: ""
            )
        } catch (e: Exception) {
            println(e.message ?: "Something went wrong")
            return null
        }
    }

    override fun convertAll(lines: List<String>): List<GfxInfoFrameStatsRecord> {
        return emptyList()
    }

}