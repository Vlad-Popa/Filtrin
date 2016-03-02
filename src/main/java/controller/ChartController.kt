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

package controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.converter.NumberStringConverter
import org.apache.commons.math3.util.Precision
import org.controlsfx.control.RangeSlider
import java.net.URL
import java.util.*

/**
 * @author Vlad Popa on 7/24/2015.
 */
class ChartController : Initializable {

    @FXML private val chart: LineChart<Number, Number>? = null
    @FXML private val xAxis: NumberAxis? = null
    @FXML private val yAxis: NumberAxis? = null
    @FXML private val xSlider: RangeSlider? = null
    @FXML private val ySlider: RangeSlider? = null
    @FXML private val chartBox: VBox? = null
    @FXML private val chartPane: HBox? = null

    private var modifier: Boolean = false
    private var insets1: Insets? = null
    private var insets2: Insets? = null
    private var insets3: Insets? = null
    private var insets4: Insets? = null

    override fun initialize(location: URL, resources: ResourceBundle) {
        insets1 = Insets(0.0, 43.0, 0.0, 38.0)
        insets2 = Insets(0.0, 57.0, 0.0, 38.0)
        insets3 = Insets(0.0, 43.0, 0.0, 40.0)
        insets4 = Insets(0.0, 57.0, 0.0, 40.0)
        yAxis!!.label = "Beta-Factor Average"
        xAxis!!.label = "Residue number"
        xAxis.tickLabelFormatter = NumberStringConverter("#")
        yAxis.tickLabelFormatter = NumberStringConverter("#0.0")
        xSlider!!.lowValueProperty().bindBidirectional(xAxis.lowerBoundProperty())
        ySlider!!.lowValueProperty().bindBidirectional(yAxis.lowerBoundProperty())
        xSlider.highValueProperty().bindBidirectional(xAxis.upperBoundProperty())
        ySlider.highValueProperty().bindBidirectional(yAxis.upperBoundProperty())
        chartBox!!.children.remove(xSlider)
        chartPane!!.children.remove(ySlider)
    }

    @FXML
    private fun handleScroll(event: ScrollEvent) {
        val deltaY = event.deltaY
        if (modifier) {
            if (deltaY > 0) {
                xSlider!!.incrementLowValue()
                xSlider.decrementHighValue()
            } else {
                xSlider!!.decrementLowValue()
                xSlider.incrementHighValue()
            }
        } else if (deltaY < 0) {
            if (xAxis!!.upperBound < xSlider!!.max) {
                xSlider.incrementLowValue()
                xSlider.incrementHighValue()
            }
        } else if (xAxis!!.lowerBound > xSlider!!.min) {
            xSlider.decrementLowValue()
            xSlider.decrementHighValue()
        }
    }

    fun getChart(): LineChart<Number, Number>? {
        return chart
    }

    fun setBounds(values: DoubleArray) {
        val xTickUnit = Precision.round((0.1 * values[1]), -1)
        val yTickUnit = Precision.round((0.2 * values[3]), 0)
        xSlider!!.min = values[0]
        ySlider!!.min = values[2]
        xSlider.max = values[1]
        ySlider.max = values[3]
        xSlider.lowValue = values[0]
        ySlider.lowValue = values[2]
        xSlider.highValue = values[1]
        ySlider.highValue = values[3]

        xAxis!!.tickUnit = xTickUnit
        yAxis!!.tickUnit = yTickUnit
    }

    fun setModifier(value: Boolean) {
        modifier = value
    }

    fun showVSlider(value: Boolean) {
        if (value) {
            chartPane!!.children.remove(ySlider)
        } else {
            chartPane!!.children.add(ySlider)
        }
    }

    fun showHSlider(value: Boolean) {
        if (value) {
            chartBox!!.children.remove(xSlider)
        } else {
            chartBox!!.children.add(xSlider)
        }
    }

    fun setPadding(b: Boolean, value: Boolean) {
        if (value && b) {
            xSlider!!.padding = insets2
        } else if (value) {
            xSlider!!.padding = insets1
        } else if (b) {
            xSlider!!.padding = insets4
        } else {
            xSlider!!.padding = insets3
        }
    }
}
