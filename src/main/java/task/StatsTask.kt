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

import application.Wrapper
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Multimap
import javafx.scene.chart.XYChart
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import java.util.concurrent.Callable

/**
 * @author Vlad Popa on 9/2/2015.
 */
class StatsTask(private val multimap: Multimap<String, XYChart.Series<Number, Number>>) : Callable<Wrapper> {

    @Throws(Exception::class)
    override fun call(): Wrapper {
        //note to self, use power set......
        // I suppose the logic here is that I go through the process of creating summerry statistics
        // objects for every chain then i powerset the summaery justatistics, then i
        // also have a powerset of thet chains. THen i iterate through both of them at the same time
        // and add to a table the min max mean and standard deviation of all of them.......
        /// i also do this for whatever.....okay that seems like the proper thing to do here....awesome...


        val table = HashBasedTable.create<String, String, SummaryStatistics>()
        for (key in multimap.keySet()) {
            val doubles = doubleArrayOf()
            val normals = StatUtils.normalize(doubles)
            this.convertDataToSeries(doubles, key)
            this.convertDataToSeries(normals, key + "N")
        }
        return Wrapper(multimap, table)
    }

    private fun convertDataToSeries(values: DoubleArray, key: String) {
        val stats = SummaryStatistics()
        val series = XYChart.Series<Number, Number>()
        var i = 0.0
        for (value in values) {
            if (value !== 0.0) {
                stats.addValue(value)
                series.data.add(XYChart.Data(i, value))
            }
            i++
        }
        multimap.put(key, series)
    }
}