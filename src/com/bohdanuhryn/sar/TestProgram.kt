package com.bohdanuhryn.sar

import com.bohdanuhryn.sar.core.datasets.IntermediateRecord
import com.bohdanuhryn.sar.core.datasets.SparseRecord
import com.bohdanuhryn.sar.core.datasets.TimeSeriesGenerator
import com.bohdanuhryn.sar.core.parsers.*
import com.bohdanuhryn.sar.datasets.logs.dumpsys.converters.GfxInfoFrameStatsRecordConverter
import com.bohdanuhryn.sar.datasets.logs.dumpsys.converters.GfxInfoFrameTimingRecordConverter
import com.bohdanuhryn.sar.datasets.logs.dumpsys.converters.GraphicsStatsRecordConverter
import com.bohdanuhryn.sar.datasets.logs.dumpsys.converters.MemInfoProcRecordConverter
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GfxInfoFrameStatsRecord
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GfxInfoFrameTimingRecord
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.GraphicsStatsRecord
import com.bohdanuhryn.sar.datasets.logs.dumpsys.models.MemInfoProcRecord
import com.bohdanuhryn.sar.datasets.logs.logcat.converters.LCActivityRecordConverter
import com.bohdanuhryn.sar.datasets.logs.logcat.converters.LCGarbageCollectorRecordConverter
import com.bohdanuhryn.sar.datasets.logs.logcat.models.LCActivityRecord
import com.bohdanuhryn.sar.datasets.logs.logcat.models.LCGarbageCollectorRecord
import com.bohdanuhryn.sar.datasets.logs.proc.converters.ProcTaskRecordConverter
import com.bohdanuhryn.sar.datasets.logs.proc.models.ProcTaskRecord
import com.bohdanuhryn.sar.datasets.timeseries.PackageMetricsRecord
import com.bohdanuhryn.sar.datasets.timeseries.UserPerceivedResponseMetricsRecord
import com.bohdanuhryn.sar.datasets.timeseries.intermediates.*
import com.bohdanuhryn.sar.utils.csv.CsvWriter
import kotlin.collections.ArrayList

class TestProgram {

    fun run() {
        //buildPackageMetricsTimeSeriesNativeApps()

        //buildPackageMetricsTimeSeriesDefaultApps()

        //buildUserPerceivedResponseMetricsTimeSeries()

        testProcTasks()
        //testLogcatGarbageCollector()
        //testTimeSeries()
        //testSparseRecord()
        //testLogcatActivity()
        //testGfxInfo()
        //testGraphicsStats()
        //testMemInfoProc()
    }

    private fun buildPackageMetricsTimeSeriesNativeApps() {
        val packages = listOf(
            "com.google.android.youtube",
            "com.android.chrome",
            "com.android.camera",
            "com.google.android.apps.photos"
        )
        val outputName = "NvsCP-NativeApps"
        buildPackageMetricsTimeSeries(outputName, packages)
    }

    private fun buildPackageMetricsTimeSeriesDefaultApps() {
        val packages = listOf(
            "com.google.android.youtube",
            "com.android.contacts",
            "com.android.chrome",
            "com.android.camera",
            "com.google.android.apps.photos"
        )
        val outputName = "DefaultApps"
        buildPackageMetricsTimeSeries(outputName, packages)
    }

    private fun buildPackageMetricsTimeSeries(outputName: String, packages: List<String>) {
        val records: ArrayList<LogRecord> = ArrayList()
        packages.forEach { packageName ->
            val converters: List<MultiLineRecordConverter<*>> = listOf(GfxInfoFrameTimingRecordConverter(false))
            val path = "./data/gfxinfo-$packageName.txt"
            val parser = MultiLineLogsParser(path, converters)
            records.addAll(parser.parse())
        }

        packages.forEach { packageName ->
            val converters: List<MultiLineRecordConverter<*>> = listOf(GfxInfoFrameStatsRecordConverter())
            val path = "./data/gfxinfo-$packageName.txt"
            val parser = MultiLineLogsParser(path, converters)
            records.addAll(parser.parse())
        }

        val memInfoPath = "./data/meminfo.txt"
        val memInfoConverters: List<MultiLineRecordConverter<*>> = listOf(MemInfoProcRecordConverter())
        val memInfoParser = MultiLineLogsParser(memInfoPath, memInfoConverters)
        records.addAll(memInfoParser.parse())

        val logcatPath = "./data/logcat.txt"
        val logCatActivityParser: List<SingleLineRecordConverterBuilder<*>> = listOf(
            { line: String -> LCActivityRecordConverter(line) }
        )
        val logCatGarbageCollectorParser: List<SingleLineRecordConverterBuilder<*>> = listOf(
            { line: String -> LCGarbageCollectorRecordConverter(line) }
        )
        val logcatActivityParser = SingleLineLogsParser(logcatPath, logCatActivityParser)
        val logcatGarbageCollectorParser = SingleLineLogsParser(logcatPath, logCatGarbageCollectorParser)
        records.addAll(logcatActivityParser.parse())
        records.addAll(logcatGarbageCollectorParser.parse())

        val intermediateBuilders = listOf<IntermediateRecord.RecordBuilder<*, *>>(
            RamUsageRecord.Builder(),
            FrameTimeRecord.Builder(),
            LaunchTimeRecord.Builder(),
            GarbageCollectorRecord.Builder(),
            FrameStatsRecord.GfxInfoBuilder()
        )

        val intermediateRecords: List<IntermediateRecord> = records.mapNotNull {
            when (it) {
                is MemInfoProcRecord -> intermediateBuilders.filterIsInstance(RamUsageRecord.Builder::class.java).first().build(it)
                is GfxInfoFrameTimingRecord -> intermediateBuilders.filterIsInstance(FrameTimeRecord.Builder::class.java).first().build(it)
                is LCActivityRecord -> intermediateBuilders.filterIsInstance(LaunchTimeRecord.Builder::class.java).first().build(it)
                is LCGarbageCollectorRecord -> intermediateBuilders.filterIsInstance(GarbageCollectorRecord.Builder::class.java).first().build(it)
                is GfxInfoFrameStatsRecord -> intermediateBuilders.filterIsInstance(FrameStatsRecord.GfxInfoBuilder::class.java).first().build(it)
                else -> null
            }
        }.toList()

        val timeSeriesBuilder = PackageMetricsRecord.Builder()
        val timeSeriesRecords = TimeSeriesGenerator(30, intermediateRecords).generate(timeSeriesBuilder)

        val outputCsvPath = "./data/output/PackageMetrics$outputName.csv"
        val csvWriter = CsvWriter(outputCsvPath, timeSeriesRecords)
        csvWriter.write(PackageMetricsRecord.MetaData().hideFields())
    }

    private fun buildUserPerceivedResponseMetricsTimeSeries() {
        val packages = listOf(
            "com.google.android.youtube",
            "com.android.contacts",
            "com.android.chrome",
            "com.android.camera",
            "com.google.android.apps.photos"
        )
        val records: ArrayList<LogRecord> = ArrayList()
        packages.forEach { packageName ->
            val converters: List<MultiLineRecordConverter<*>> = listOf(GfxInfoFrameTimingRecordConverter(false))
            val path = "./data/gfxinfo-$packageName.txt"
            val parser = MultiLineLogsParser(path, converters)
            records.addAll(parser.parse())
        }

        packages.forEach { packageName ->
            val converters: List<MultiLineRecordConverter<*>> = listOf(GfxInfoFrameStatsRecordConverter())
            val path = "./data/gfxinfo-$packageName.txt"
            val parser = MultiLineLogsParser(path, converters)
            records.addAll(parser.parse())
        }

        val memInfoPath = "./data/meminfo.txt"
        val memInfoConverters: List<MultiLineRecordConverter<*>> = listOf(MemInfoProcRecordConverter())
        val memInfoParser = MultiLineLogsParser(memInfoPath, memInfoConverters)
        records.addAll(memInfoParser.parse())

        val logcatPath = "./data/logcat.txt"
        val logCatActivityParser: List<SingleLineRecordConverterBuilder<*>> = listOf(
            { line: String -> LCActivityRecordConverter(line) }
        )
        val logcatParser = SingleLineLogsParser(logcatPath, logCatActivityParser)
        records.addAll(logcatParser.parse())

        /*val graphicsStatsPath = "./data/graphicsstats.txt"
        val graphicsStatsConverters: List<MultiLineRecordConverter<*>> = listOf(GraphicsStatsRecordConverter())
        val graphicsStatsParser = MultiLineLogsParser(graphicsStatsPath, graphicsStatsConverters)
        records.addAll(graphicsStatsParser.parse())*/

        val intermediateBuilders = listOf<IntermediateRecord.RecordBuilder<*, *>>(
            RamUsageRecord.Builder(),
            FrameTimeRecord.Builder(),
            LaunchTimeRecord.Builder(),
            FrameStatsRecord.GfxInfoBuilder()
        )

        val intermediateRecords: List<IntermediateRecord> = records.mapNotNull {
            when (it) {
                is MemInfoProcRecord -> intermediateBuilders.filterIsInstance(RamUsageRecord.Builder::class.java).first().build(it)
                is GfxInfoFrameTimingRecord -> intermediateBuilders.filterIsInstance(FrameTimeRecord.Builder::class.java).first().build(it)
                is LCActivityRecord -> intermediateBuilders.filterIsInstance(LaunchTimeRecord.Builder::class.java).first().build(it)
                is GfxInfoFrameStatsRecord -> intermediateBuilders.filterIsInstance(FrameStatsRecord.GfxInfoBuilder::class.java).first().build(it)
                else -> null
            }
        }.toList()

        val timeSeriesBuilder = UserPerceivedResponseMetricsRecord.Builder()
        val timeSeriesRecords = TimeSeriesGenerator(30, intermediateRecords).generate(timeSeriesBuilder)

        val outputCsvPath = "./data/output/UserPerceivedResponseMetrics.csv"
        val csvWriter = CsvWriter(outputCsvPath, timeSeriesRecords)
        csvWriter.write(UserPerceivedResponseMetricsRecord.MetaData().hideFields())
    }

    private fun testTimeSeries() {
        val packages = listOf(
            "com.android.contacts",
            "com.google.android.calendar"
        )
        val records: ArrayList<LogRecord> = ArrayList()
        packages.forEach { packageName ->
            val converters: List<MultiLineRecordConverter<*>> = listOf(GfxInfoFrameTimingRecordConverter(true))
            val testPath = "./data/gfxinfo-$packageName.txt"
            val parser = MultiLineLogsParser(testPath, converters)
            records.addAll(parser.parse())
        }

        val memInfoPath = "./data/meminfo.txt"
        val memInfoConverters: List<MultiLineRecordConverter<*>> = listOf(
            MemInfoProcRecordConverter()
        )
        val memInfoParser = MultiLineLogsParser(memInfoPath, memInfoConverters)
        records.addAll(memInfoParser.parse())

        val logcatPath = "./data/logcat.txt"

        val logCatActivityParser: List<SingleLineRecordConverterBuilder<*>> = listOf(
            { line: String -> LCActivityRecordConverter(line) }
        )
        val logcatParser = SingleLineLogsParser(logcatPath, logCatActivityParser)
        records.addAll(logcatParser.parse())

        //

        val intermediateBuilders = listOf<IntermediateRecord.RecordBuilder<*, *>>(
            RamUsageRecord.Builder(),
            FrameTimeRecord.Builder(),
            LaunchTimeRecord.Builder()
        )

        val intermediateRecords: List<IntermediateRecord> = records.mapNotNull {
            when (it) {
                is MemInfoProcRecord -> intermediateBuilders.filterIsInstance(RamUsageRecord.Builder::class.java).first().build(it)
                is GfxInfoFrameTimingRecord -> intermediateBuilders.filterIsInstance(FrameTimeRecord.Builder::class.java).first().build(it)
                is LCActivityRecord -> intermediateBuilders.filterIsInstance(LaunchTimeRecord.Builder::class.java).first().build(it)
                else -> null
            }
        }.toList()

        val timeSeriesBuilder = UserPerceivedResponseMetricsRecord.Builder()
        val timeSeriesRecords = TimeSeriesGenerator(30, intermediateRecords).generate(timeSeriesBuilder)

        val outputCsvPath = "./data/output/timeseries.csv"
        val csvWriter = CsvWriter(outputCsvPath, timeSeriesRecords)
        csvWriter.write(UserPerceivedResponseMetricsRecord.MetaData().hideFields())
    }

    private fun testSparseRecord() {
        val packages = listOf(
            "com.android.contacts",
            "com.google.android.calendar"
        )
        val records: ArrayList<LogRecord> = ArrayList()
        packages.forEach { packageName ->
            val converters: List<MultiLineRecordConverter<*>> = listOf(GfxInfoFrameTimingRecordConverter(true))
            val testPath = "./data/gfxinfo-$packageName.txt"
            val parser = MultiLineLogsParser(testPath, converters)
            records.addAll(parser.parse())
        }

        val memInfoTestPath = "./data/meminfo.txt"
        val memInfoConverters: List<MultiLineRecordConverter<*>> = listOf(
            MemInfoProcRecordConverter()
        )
        val parser = MultiLineLogsParser(memInfoTestPath, memInfoConverters)
        records.addAll(parser.parse())

        val intermediateBuilders = listOf<IntermediateRecord.RecordBuilder<*, *>>(
            RamUsageRecord.Builder(),
            FrameTimeRecord.Builder()
        )
        val intermediateMetaData = listOf(
            RamUsageRecord.MetaData(),
            FrameTimeRecord.MetaData()
        )
        val sparseRecords = records.mapNotNull {
            val s = when (it) {
                is MemInfoProcRecord -> intermediateBuilders.filterIsInstance(RamUsageRecord.Builder::class.java).first().build(it)
                is GfxInfoFrameTimingRecord -> intermediateBuilders.filterIsInstance(FrameTimeRecord.Builder::class.java).first().build(it)
                else -> null
            }
            if (s != null) {
                SparseRecord(intermediateMetaData, s)
            } else {
                null
            }
        }.toList()
        val sparseOutputCsvPath = "./data/output/sparse.csv"
        val sparseCsvWriter = CsvWriter(sparseOutputCsvPath, sparseRecords)
        sparseCsvWriter.write()
    }

    private fun testLogcatActivity() {
        val logcatTestPath = "./data/logcat.txt"
        val logcatOutputCsvPath = "./data/output/logcat.csv"

        val parsers: List<SingleLineRecordConverterBuilder<*>> = listOf(
            { line: String -> LCActivityRecordConverter(line) }
        )
        val logcatParser = SingleLineLogsParser(logcatTestPath, parsers)
        val records = logcatParser.parse()
        records.forEach(::println)

        val logcatCsvWriter = CsvWriter(logcatOutputCsvPath, records.filterIsInstance<LCActivityRecord>().toList())
        logcatCsvWriter.write()
    }

    private fun testLogcatGarbageCollector() {
        val logcatTestPath = "./data/logcat.txt"
        val logcatOutputCsvPath = "./data/output/logcatGC.csv"

        val parsers: List<SingleLineRecordConverterBuilder<*>> = listOf(
            { line: String -> LCGarbageCollectorRecordConverter(line) }
        )
        val logcatParser = SingleLineLogsParser(logcatTestPath, parsers)
        val records = logcatParser.parse()
        records.forEach(::println)

        val logcatCsvWriter = CsvWriter(logcatOutputCsvPath, records.filterIsInstance<LCGarbageCollectorRecord>().toList())
        logcatCsvWriter.write()
    }

    private fun testGfxInfo() {
        val packages = listOf(
            "com.android.contacts",
            "com.google.android.calendar"
        )

        packages.forEach { packageName ->
            val testPath = "./data/gfxinfo-$packageName.txt"
            val outputCsvPath = "./data/output/gfxinfo-$packageName.csv"
            val converters: List<MultiLineRecordConverter<*>> = listOf(GfxInfoFrameTimingRecordConverter(true))
            val parser = MultiLineLogsParser(testPath, converters)
            val records = parser.parse()
            records.forEach(::println)

            val csvWriter = CsvWriter(outputCsvPath, records.filterIsInstance<GfxInfoFrameTimingRecord>().toList())
            csvWriter.write()
        }
    }

    private fun testGraphicsStats() {
        val converters: List<MultiLineRecordConverter<*>> = listOf(
            GraphicsStatsRecordConverter()
        )
        val testPath = "./data/graphicsstats.txt"
        val outputCsvPath = "./data/output/graphicsstats.csv"

        val parser = MultiLineLogsParser(testPath, converters)
        val records = parser.parse()
        records.forEach(::println)

        val csvWriter = CsvWriter(outputCsvPath, records.filterIsInstance<GraphicsStatsRecord>().toList())
        csvWriter.write()
    }

    private fun testMemInfoProc() {
        val converters: List<MultiLineRecordConverter<*>> = listOf(
            MemInfoProcRecordConverter()
        )
        val testPath = "./data/meminfo.txt"
        val outputCsvPath = "./data/output/meminfoproc.csv"

        val parser = MultiLineLogsParser(testPath, converters)
        val records = parser.parse()
        records.forEach(::println)

        val csvWriter = CsvWriter(outputCsvPath, records.filterIsInstance<MemInfoProcRecord>().toList())
        csvWriter.write()
    }

    private fun testProcTasks() {
        val testPath = "./data/proctasks.txt"
        val outputCsvPath = "./data/output/proctasks.csv"

        val parsers: List<SingleLineRecordConverterBuilder<*>> = listOf(
            { line: String -> ProcTaskRecordConverter(line) }
        )
        val parser = SingleLineLogsParser(testPath, parsers)
        val records = parser.parse()
        records.forEach(::println)

        val csvWriter = CsvWriter(outputCsvPath, records.filterIsInstance<ProcTaskRecord>().toList())
        csvWriter.write()
    }

}