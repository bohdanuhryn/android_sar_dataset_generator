package com.bohdanuhryn.sar.datasets.logs.logcat.converters

import com.bohdanuhryn.sar.core.parsers.SingleLineRecordConverter
import com.bohdanuhryn.sar.datasets.logs.logcat.models.LCActivityRecord
import java.util.regex.Matcher
import java.util.regex.Pattern

//
// 03-06 03:21:33.352  1604  1625 I ActivityManager: Displayed com.google.android.calendar/.launch.oobe.WhatsNewFullScreen: +1s306ms (total +2s376ms)
//
// 1301331.400  1604  1625 I ActivityManager: Displayed com.google.android.calendar/.launch.oobe.WhatsNewFullScreen: +1s26ms (total +1s216ms)
//

class LCActivityRecordConverter(
    text: String
) : SingleLineRecordConverter<LCActivityRecord>() {

    protected val TIMESTAMP_REGEX = "(\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d)"
    protected val TIMESTAMP_MONOTONIC_REGEX = "(\\d+\\.\\d\\d\\d)"
    protected val ID_REGEX = "(\\d+)"
    protected val PID_REGEX = ID_REGEX
    protected val TID_REGEX = ID_REGEX

    protected val TAG_REGEX = "(.*?)"
    protected val PRIORITY_REGEX = "([AVDIWEF])"
    protected val MESSAGE_REGEX = "(.*)"
    protected val SEP = "\\s+"
    protected val SEP_OPT = "\\s*"

    private val TAG = "(ActivityManager):"
    private val DISPLAYED_STATUS = "Displayed"
    private val MESSAGE = "$MESSAGE_REGEX:"
    private val TIME_REGEX = "(.*)"
    private val TOTAL_TIME_REGEX = "\\(total (.*)\\)"

    private val fields = arrayOf(
        TIMESTAMP_MONOTONIC_REGEX, SEP,
        PID_REGEX, SEP,
        TID_REGEX, SEP,
        PRIORITY_REGEX, SEP,
        TAG, SEP,
        DISPLAYED_STATUS, SEP,
        MESSAGE, SEP,
        TIME_REGEX, SEP,
        TOTAL_TIME_REGEX
    )

    private val pattern: Pattern = Pattern.compile("^" + fields.fold("", { s, f -> "$s$f" }) + "$")

    private val matcher: Matcher = pattern.matcher(text)

    override fun isValid(): Boolean {
        return matcher.matches()
    }

    override fun convert(): LCActivityRecord? {
        if (isValid()) {
            val timestamp: String = matcher.group(1)
            val pid: Int = matcher.group(2).toInt()
            val tid: Int = matcher.group(3).toInt()
            val priority: String = matcher.group(4)
            val tag: String = matcher.group(5)
            val packageName: String = matcher.group(6)
            val time: String = matcher.group(7)
            val totalTime: String = matcher.group(8)
            return LCActivityRecord(
                timestamp = timestamp,
                pid = pid,
                tid = tid,
                priority = priority,
                tag = tag,
                packageName = packageName,
                time = time,
                totalTime = totalTime
            );
        } else {
            return null
        }
    }
}