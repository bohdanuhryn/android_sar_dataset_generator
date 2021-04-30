package com.bohdanuhryn.sar.utils.csv

interface CsvRow {

    fun getCsvCells(): List<String>

    fun getCsvColumns(): List<String>

}