/*
 * Copyright (C) 2015 Vlad Popa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package task

import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.poi.ss.usermodel.Sheet
import java.util.concurrent.BlockingQueue

/**
 * @author Vlad Popa on 7/1/2015.
 */
class WriteTask(private val queue: BlockingQueue<String>,
                private val sheet: Sheet,
                private val includeH: Boolean) : Runnable {

    override fun run() {
        var index = 1
        var column = 0
        var chainId = " "
        val statistics = DescriptiveStatistics()
        while (true) {
            val line = queue.take()
            if (!line.equals("POISON")) {
                val atomName   = line.substring(12, 16)
                val resName    = line.substring(17, 20)
                val currentId  = line.substring(21, 22)
                val resSeq     = line.substring(23, 26).toDouble()
                val tempFactor = line.substring(60, 66).toDouble()
                val isHydrogen = line[77] == 'H'
                if (chainId == " ") {
                    chainId = currentId
                } else if (chainId != currentId) {
                    val array = StatUtils.normalize(statistics.values)
                    for (j in 1..index) {
                        var row = sheet.getRow(j)
                        if (row == null) row = sheet.createRow(j)
                        row.createCell(column + 5).setCellValue(array[j])
                    }
                    method1(column, statistics)
                    statistics.clear()
                    chainId = currentId
                    column += 8
                    index = 1
                }
                statistics.addValue(tempFactor)
                if ((isHydrogen && includeH) || !isHydrogen) {
                    var row = sheet.getRow(index)
                    if (row == null) row = sheet.createRow(index)
                    row.createCell(column + 0).setCellValue(atomName)
                    row.createCell(column + 1).setCellValue(resName)
                    row.createCell(column + 2).setCellValue(currentId)
                    row.createCell(column + 3).setCellValue(resSeq)
                    row.createCell(column + 4).setCellValue(tempFactor)
                    index++
                }
            } else break
        }
    }

    private fun method1(column: Int, statistics: DescriptiveStatistics) {
        sheet.getRow(0).createCell(column + 0).setCellValue("Chain")
        sheet.getRow(0).createCell(column + 1).setCellValue("Sequence Number")
        sheet.getRow(0).createCell(column + 2).setCellValue("Residue")
        sheet.getRow(0).createCell(column + 3).setCellValue("Atom")
        sheet.getRow(0).createCell(column + 4).setCellValue("Temperature Factor")
        sheet.getRow(0).createCell(column + 5).setCellValue("Normalized Temperature Factor")
        sheet.getRow(0).createCell(column + 6).setCellValue("Mean")
        sheet.getRow(0).createCell(column + 7).setCellValue("Standard Deviation")
        sheet.getRow(1).createCell(column + 6).setCellValue(statistics.mean)
        sheet.getRow(1).createCell(column + 7).setCellValue(statistics.standardDeviation)
    }
}
