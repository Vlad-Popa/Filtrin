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

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Multimap
import com.google.common.collect.Table
import javafx.scene.chart.XYChart
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import java.util.concurrent.Callable

/**
 * @author Vlad Popa on 9/2/2015.
 */
class StatsTask(private val multimap: Multimap<String, XYChart.Series<Number, Number>>) :
        Callable<Table<String, String, SummaryStatistics>> {

    @Throws(Exception::class)
    override fun call(): HashBasedTable<String, String, SummaryStatistics>? {
        val table = HashBasedTable.create<String, String, SummaryStatistics>()
        for (key in multimap.keys()) {
            for (series in multimap.get(key)) {
                val statistics = SummaryStatistics()
                for (data in series.data) {
                    statistics.addValue(data.yValue.toDouble());
                }
                table.put(key, series.name, statistics)
            }
        }
        return table
    }
}