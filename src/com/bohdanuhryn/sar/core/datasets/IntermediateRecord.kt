package com.bohdanuhryn.sar.core.datasets

abstract class IntermediateRecord(
    val timeStampMs: Long,
    val tag: String
) {

    abstract fun getRow(): List<String>

    abstract class RecordMetaData {

        abstract fun getTag(): String

        abstract fun getColumnsCount(): Int

        abstract fun getColumnPrefix(): String

        abstract fun getColumnPostfixes(): List<String>

        abstract fun getColumnNames(): List<String>

        fun getColumnNamesFull(): List<String> {
            val postfixes = getColumnPostfixes()
            return getColumnNames()
                .mapIndexed { index, name ->
                    val postfix = postfixes.getOrElse(index) { "" }
                    var fullName = "${getColumnPrefix()}_$name"
                    if (postfix.isNotBlank()) {
                        fullName = "${fullName}_$postfix"
                    }
                    fullName
                }
                .toList()
        }

    }

    interface RecordBuilder<IR, OR : IntermediateRecord> {

        fun build(origin: IR): OR

    }

}
