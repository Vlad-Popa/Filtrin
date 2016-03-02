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

package application

import com.google.common.collect.Collections2
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.google.common.collect.Table
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.chart.XYChart
import javafx.scene.control.TableView

import java.io.File

/**
 * @author Vlad Popa on 9/1/2015.
 */
class Model private constructor(builder: Model.Builder) {

    private val pdb: SimpleStringProperty
    private val avg: SimpleDoubleProperty
    private val std: SimpleDoubleProperty
    private val min: SimpleDoubleProperty
    private val max: SimpleDoubleProperty

    public val file: File
    public var set: MutableSet<String>
    public val key: String
    public val view: TableView<List<String>>
    public val series: Multimap<String, XYChart.Series<Number, Number>>
    public val statistics: Table<String, String, Double>

    private val lower: Int
    private val upper: Int

    class Builder {

        private var series: Multimap<String, XYChart.Series<Number, Number>>? = null
        private var statistics: Table<String, String, Double>? = null
        private var table: TableView<List<String>>? = null
        private var file: File? = null
        private var name: String? = null
        private var minima: Int = 0
        private var maxima: Int = 0

        fun setSeries(series: Multimap<String, XYChart.Series<Number, Number>>) {
            this.series = series
        }

        fun setStatistics(statistics: Table<String, String, Double>) {
            this.statistics = statistics
        }

        fun setTable(tableView: TableView<List<String>>) {
            this.table = tableView
        }

        fun setLimits(min: Int, max: Int) {
            this.minima = min
            this.maxima = max
        }

        fun setFile(file: File) {
            this.name = file.name
            this.file = file
        }

        fun build(): Model {
            return Model(this)
        }
    }

    init {
        lower = builder.minima
        upper = builder.maxima
        file = builder.file
        series = builder.series
        view = builder.table
        statistics = builder.statistics
        set = Sets.newHashSet(statistics.columnKeySet())
        pdb = SimpleStringProperty(builder.name)
        min = SimpleDoubleProperty()
        max = SimpleDoubleProperty()
        avg = SimpleDoubleProperty()
        std = SimpleDoubleProperty()
    }

    val name: String
        get() = file.name

    fun getSeries(key: String): Collection<XYChart.Series<Number, Number>> {
        val collection = series.get(key)
        return Collections2.filter(collection) { xy -> set.contains(xy.name) }
    }

    val bounds: DoubleArray
        get() = doubleArrayOf(lower.toDouble(), upper.toDouble(), min.get(), max.get())

    fun getSet(): Set<String> {
        return Sets.newHashSet(statistics.columnKeySet())
    }

    fun refreshSet() {
        set = Sets.newHashSet(statistics.columnKeySet())
    }

    fun updateSet(newValue: Boolean, value: String) {
        if (newValue) {
            set.add(value)
        } else {
            set.remove(value)
        }
    }

    fun pdbProperty(): SimpleStringProperty {
        return pdb
    }

    fun avgProperty(): SimpleDoubleProperty {
        return avg
    }

    fun stdProperty(): SimpleDoubleProperty {
        return std
    }

    fun minProperty(): SimpleDoubleProperty {
        return min
    }

    fun maxProperty(): SimpleDoubleProperty {
        return max
    }
}
