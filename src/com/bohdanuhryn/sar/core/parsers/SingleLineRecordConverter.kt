package com.bohdanuhryn.sar.core.parsers

abstract class SingleLineRecordConverter<T : LogRecord> {

    abstract fun isValid(): Boolean

    abstract fun convert(): T?

}

typealias SingleLineRecordConverterBuilder<T> = (String) -> SingleLineRecordConverter<T>