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

import com.google.common.collect.Ordering
import com.google.common.collect.TreeMultimap
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch

/**
 * @author Vlad Popa on 7/17/2015.
 */
class DataTask(private val queue: BlockingQueue<String>,
               private val latch: CountDownLatch) : Runnable {

    private val data = arrayListOf<ObservableList<XYChart.Data<Number, Number>>>()
    private val series = TreeMultimap.create(Ordering.natural<String>(), Ordering.usingToString())
    private var hTotal = 0.0
    private var rTotal = 0.0
    private var hCount = 0.0
    private var rCount = 0.0

    override fun run() {
        val values = doubleArrayOf();
        for (i in 0..6) {
            data.add(FXCollections.observableArrayList<XYChart.Data<Number, Number>>())
        }
        var min = 0
        var max = 0
        var resSeq = 0
        var chainId = ' '
        while (true) {
            val line = queue.take()
            if (!line.equals("POISON")) {
                if (line[16] < 'B') {
                    val atomName   = line.substring(12, 16).trim()
                    val tempFactor = line.substring(60, 66).toDouble()
                    val currentId  = line[21]
                    if (chainId == ' ') {
                        chainId = currentId
                        min = Math.min(min, resSeq)
                    } else if (line[77] == 'H') {
                        hTotal += tempFactor
                        hCount += 1
                    } else when (atomName) {
                        "N"  -> method1(values, resSeq)
                        "CA" -> values[4] = tempFactor
                        "C"  -> values[5] = (rTotal + tempFactor) / 3
                        "O"  -> values[6] = (rTotal + tempFactor) / 4
                    }
                    rCount += 1
                    rTotal += tempFactor
                    resSeq  = line.substring(23, 26).toInt()
                    if (chainId != currentId) {
                        method2(chainId.toString())
                        min = Math.min(min, resSeq)
                        max = Math.max(max, resSeq)
                        chainId = currentId
                    }
                }
            } else break
        }
        min -= (10 + (min % 10))
        max += (10 - (max % 10))
        method1(values, resSeq)
        method2(chainId.toString())
        latch.countDown()
    }

    private fun method1(values: DoubleArray, resSeq: Int) {
        values[0] = (rTotal / rCount)
        values[1] = (rTotal - hTotal) / (rCount - hCount)
        values[2] = (rTotal - values[6]) / (rCount - 3)
        values[3] = (rTotal - values[6] - hTotal) / (rCount - hCount - 3)
        for (i in values.indices) {
            data[i].add(XYChart.Data(resSeq, values[i]))
        }
        hTotal = 0.0
        rTotal = 0.0
        hCount = 0.0
        rCount = 0.0
    }

    private fun method2(chainId: String) {
        for (i in 0..7) {
            val key = method3(i);
            series.put(key, XYChart.Series(chainId, data[i]))
            data[i].clear()
        }
    }

    private fun method3(index : Int) : String {
        var toReturn = "";
        when (index) {
            0 -> toReturn = "All atomsH"
            1 -> toReturn = "All atoms"
            2 -> toReturn = "Side chainH"
            3 -> toReturn = "Side chain"
            4 -> toReturn = "C-Alpha"
            5 -> toReturn = "Backbone"
            6 -> toReturn = "Main chain"
        }
        return toReturn
    }
}