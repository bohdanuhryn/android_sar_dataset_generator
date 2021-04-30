package com.bohdanuhryn.sar.datasets.logs.logcat.converters

import com.bohdanuhryn.sar.core.parsers.SingleLineRecordConverter
import com.bohdanuhryn.sar.datasets.logs.logcat.models.LCGarbageCollectorRecord
import java.util.regex.Matcher
import java.util.regex.Pattern

//
// 132.355  1665  1674 I art     : Background partial concurrent mark sweep GC freed 56110(3MB) AllocSpace objects, 37(1452KB) LOS objects, 33% free, 32MB/48MB, paused 2.088ms total 125.289ms
//
// (\d+\.\d\d\d)\s+(\d+)\s+(\d+)\s+([AVDIWEF])\s+(art\s*):(.*)GC freed(.*)paused(\s+)(\d+\.\d\d\d)ms(.*)total(\s+)(\d+\.\d\d\d)ms
//

class LCGarbageCollectorRecordConverter(
    text: String
) : SingleLineRecordConverter<LCGarbageCollectorRecord>() {

    protected val TIMESTAMP_REGEX = "(\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d)"
    protected val TIMESTAMP_MONOTONIC_REGEX = "(\\d+\\.\\d\\d\\d)"
    protected val ID_REGEX = "(\\d+)"
    protected val PID_REGEX = ID_REGEX
    protected val TID_REGEX = ID_REGEX

    protected val PRIORITY_REGEX = "([AVDIWEF])"
    protected val SEP = "\\s+"
    protected val SEP_OPT = "\\s*"

    private val TAG = "(art\\s*):"
    private val GC_FREED = "(.*)GC freed"
    private val PAUSED = "(.*)paused"
    private val TOTAL = "(.*)total"
    private val TIME_MS_REGEX = "(\\d+\\.\\d\\d\\d)ms"

    private val fields = arrayOf(
        TIMESTAMP_MONOTONIC_REGEX, SEP,
        PID_REGEX, SEP,
        TID_REGEX, SEP,
        PRIORITY_REGEX, SEP,
        TAG, SEP,
        GC_FREED, SEP,
        PAUSED, SEP,
        TIME_MS_REGEX, SEP,
        TOTAL, SEP,
        TIME_MS_REGEX
    )

    private val pattern: Pattern = Pattern.compile("^" + fields.fold("", { s, f -> "$s$f" }) + "$")

    private val matcher: Matcher = pattern.matcher(text)

    override fun isValid(): Boolean {
        return matcher.matches()
    }

    override fun convert(): LCGarbageCollectorRecord? {
        if (isValid()) {
            val timestamp: String = matcher.group(1)
            val pid: Int = matcher.group(2).toInt()
            val tid: Int = matcher.group(3).toInt()
            val priority: String = matcher.group(4)
            val tag: String = matcher.group(5)
            val pausedTimeMs: String = matcher.group(8)
            val totalTimeMs: String = matcher.group(10)
            return LCGarbageCollectorRecord(
                timestamp = timestamp,
                pid = pid,
                tid = tid,
                priority = priority,
                tag = tag,
                pausedTimeMs = pausedTimeMs,
                totalTimeMs = totalTimeMs
            );
        } else {
            return null
        }
    }
}