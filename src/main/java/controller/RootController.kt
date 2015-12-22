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

import application.Model
import com.google.common.collect.Lists
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.DoubleProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.chart.XYChart
import javafx.scene.control.MenuBar
import javafx.scene.control.SplitPane
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleButton
import javafx.scene.input.DragEvent
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.util.Duration
import task.DataTask
import task.ListTask
import task.ReadTask

import java.net.URL
import java.util.ResourceBundle
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * @author Vlad Popa on 7/18/2015.
 */
class RootController : Initializable {

    @FXML private val root: VBox? = null
    @FXML private val table: VBox? = null
    @FXML private val vBox: VBox? = null
    @FXML private val chart: VBox? = null
    @FXML private val menu: MenuBar? = null
    @FXML private val outerPane: SplitPane? = null
    @FXML private val graphPane: SplitPane? = null
    @FXML private val menuController: MenuController? = null
    @FXML private val chartController: ChartController? = null
    @FXML private val tableController: TableController? = null

    private var buttons: ObservableList<ToggleButton>? = null
    private var series: ObservableList<XYChart.Series<Number, Number>>? = null
    private var shift: Boolean = false

    private val filter = FileNameExtensionFilter("PDB", "pdb", "txt", "ent")

    override fun initialize(location: URL, resources: ResourceBundle) {
        root!!.addEventHandler(KeyEvent.ANY) { event ->
            chartController!!.setModifier(event.isControlDown)
            shift = event.isShiftDown
        }

        series = FXCollections.observableArrayList<XYChart.Series<Number, Number>>()
        buttons = FXCollections.observableArrayList<ToggleButton>()

        Bindings.bindContentBidirectional(series, chartController!!.getChart()!!.data)
        Bindings.bindContentBidirectional(buttons, tableController!!.group)
        val divider1 = outerPane!!.dividers[0].positionProperty()
        val divider2 = graphPane!!.dividers[0].positionProperty()

        root.widthProperty().addListener { observable, oldValue, newValue ->
            if (!tableController.pToggle.get()) {
                Platform.runLater { divider1.set(1.0) }
            } else {
                Platform.runLater { divider1.set(0.8) }
            }
            if (tableController.sToggle.get()) {
                Platform.runLater { divider2.set(0.5) }
            } else {
                Platform.runLater { divider2.set(1.0) }
            }
        }

        menuController!!.selectedItem().addListener(categoryListener())
        tableController.items.addListener(ListChangeListener {
            while (it.next()) {
                for (model in it.addedSubList) {
                    model.setValues(menuController.value + tableController.value)
                }
            }
        })

        tableController.selectedItem().addListener(modelListener())
        tableController.hToggle.addListener(statisticsListener())
        tableController.nToggle.addListener(statisticsListener())
        tableController.hgLines.addListener(hGridListener())
        tableController.vgLines.addListener(vGridListener())
        tableController.pToggle.addListener(dividerListener1(divider1, 0.8, 1.0))
        tableController.sToggle.addListener(dividerListener2(divider2, 0.5, 1.0))
        tableController.hSlider.addListener(hSlideListener())
        tableController.vSlider.addListener(vSlideListener())
        tableController.pointTg.addListener(pointListener())
    }

    @FXML private fun handleDragDropped(event: DragEvent) {
        val dragboard = event.dragboard
        if (dragboard.hasFiles()) {
            for (file in dragboard.files) {
                if (filter.accept(file)) {
                    val queue1 = ArrayBlockingQueue<String>(200)
                    val queue2 = ArrayBlockingQueue<String>(200)
                    val latch  = CountDownLatch(2)

                    Thread(ReadTask(queue1, queue2, file)).start()
                    Thread(DataTask(queue1, latch)).start()
                    Thread(ListTask(queue2, latch)).start()

                    latch.await()
                }
            }
        }
        event.isDropCompleted = true
        event.consume()
    }

    private fun categoryListener(): ChangeListener<Toggle> {
        return ChangeListener { observableValue, oldValue, newValue ->
            if (newValue != null) {
                val value = menuController!!.value
                when (value) {
                    "Main chain", "Backbone", "C-Alpha" -> {
                        tableController!!.hToggle.set(false)
                        tableController.getActualHToggle().setDisable(true)
                    }
                    else -> tableController!!.getActualHToggle().setDisable(false)
                }
                if (!tableController.items.isEmpty()) {
                    Platform.runLater {
                        val model = tableController.selectedItem().get()
                        val key = value + tableController.value
                        for (item in tableController.items) {

                        }
                        series!!.setAll(model.getSeries(key))
                        chartController!!.setBounds(model.bounds)
                    }
                }
            }
        }
    }

    private fun modelListener(): ChangeListener<Model> {
        return ChangeListener { observableValue, oldValue, newValue ->
            val value = menuController!!.value
            if (!shift && !tableController!!.items.isEmpty()) {
                newValue.refreshSet()
                val key = value + tableController.value
                val toggleList = getButtons(newValue)
                Platform.runLater {
                    series!!.setAll(newValue.getSeries(key))
                    chartController!!.setBounds(newValue.bounds)
                    if (tableController.pToggle.get()) {
                        vBox!!.children.setAll(newValue.view)
                    }
                    buttons!!.setAll(toggleList)
                }
            } else if (tableController!!.items.isEmpty()) {
                series!!.clear()
                buttons!!.clear()
            }
        }
    }

    private fun vSlideListener(): ChangeListener<Boolean> {
        return ChangeListener { observableValue, oldValue, newValue ->
            chartController!!.showVSlider(oldValue!!)
            chartController.setPadding(newValue!!, tableController!!.nToggle.get())
        }
    }

    private fun hSlideListener(): ChangeListener<Boolean> {
        return ChangeListener { observableValue, oldValue, newValue -> chartController!!.showHSlider(oldValue!!) })
    }

    private fun vGridListener(): ChangeListener<Boolean> {
        return ChangeListener { observableValue, oldValue, newValue ->
            chartController!!.getChart()!!.verticalGridLinesVisible = newValue!!
            chartController.getChart()!!.isVerticalZeroLineVisible = newValue
        }
    }

    private fun hGridListener(): ChangeListener<Boolean> {
        return ChangeListener { observableValue, oldValue, newValue ->
            chartController!!.getChart()!!.isHorizontalGridLinesVisible = newValue!!
            chartController.getChart()!!.isHorizontalZeroLineVisible = newValue
        }
    }

    private fun pointListener(): ChangeListener<Boolean> {
        return ChangeListener {
            observableValue, oldValue, newValue -> chartController!!.getChart()!!.createSymbols = newValue!!
        }
    }

    private fun statisticsListener(): ChangeListener<Boolean> {
        return ChangeListener { observableValue, oldValue, newValue ->
            if (!tableController!!.items.isEmpty()) {
                val key = menuController!!.value + tableController.value
                for (item in tableController.items) {
                    item.setValues(key)
                }
                val model = tableController.selectedItem().get()
                series!!.setAll(model.getSeries(key))
                chartController!!.setBounds(model.bounds)
                val toggle = tableController.nToggle.get()
                chartController.setPadding(tableController.vSlider.get(), toggle)
            }
        }
    }

    private fun dividerListener1(divider: DoubleProperty, to: Double, from: Double): ChangeListener<Boolean> {
        return ChangeListener { observableValue, oldValue, newValue ->
            val value: KeyValue
            if (newValue!! && !tableController!!.items.isEmpty()) {
                value = KeyValue(divider, to)
                val model = tableController.selectedItem().get()
                vBox!!.children.setAll(model.view)
            } else {
                value = KeyValue(divider, from)
                vBox!!.children.clear()
            }
            Timeline(KeyFrame(Duration.seconds(0.2), value)).play()
        }
    }

    private fun dividerListener2(divider: DoubleProperty, to: Double, from: Double): ChangeListener<Boolean> {
        return ChangeListener { observableValue, oldValue, newValue ->
            val value = if (newValue)
                KeyValue(divider, to)
            else
                KeyValue(divider, from)
            Timeline(KeyFrame(Duration.seconds(0.2), value)).play()
        }
    }

    private fun getButtons(model: Model): List<ToggleButton> {
        val list = Lists.newArrayList<ToggleButton>()
        for (string in model.set) {
            val button = ToggleButton(string)
            button.setPrefSize(30.0, 30.0)
            button.isSelected = true
            button.selectedProperty().addListener { observable, oldValue, newValue ->
                model.updateSet(newValue, string)
                val key = menuController!!.value + tableController!!.value
                series!!.setAll(model.getSeries(key))
                model.setValues(key)
            }
            list.add(button)
        }
        return list
    }
}

