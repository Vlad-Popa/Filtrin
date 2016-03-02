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
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.util.StringConverter
import org.controlsfx.control.SegmentedButton
import java.net.URL
import java.text.NumberFormat
import java.util.*

/**
 * @author Vlad Popa on 7/25/2015.
 */
class TableController : Initializable {

    @FXML private val table: TableView<Model>? = null
    @FXML private val pdbColumn: TableColumn<Model, String>? = null
    @FXML private val minColumn: TableColumn<Model, Double>? = null
    @FXML private val maxColumn: TableColumn<Model, Double>? = null
    @FXML private val avgColumn: TableColumn<Model, Double>? = null
    @FXML private val stdColumn: TableColumn<Model, Double>? = null

    @FXML private val group2: SegmentedButton? = null
    @FXML private val group1: SegmentedButton? = null
    @FXML private val group3: SegmentedButton? = null
    @FXML private val hSliderToggle: ToggleButton? = null
    @FXML private val vSliderToggle: ToggleButton? = null
    @FXML private val pointToggle: ToggleButton? = null
    @FXML private val panelToggle: ToggleButton? = null
    @FXML private val actualHToggle: ToggleButton? = null
    @FXML private val normlToggle: ToggleButton? = null
    @FXML private val statsToggle: ToggleButton? = null
    @FXML private val hGridToggle: ToggleButton? = null
    @FXML private val vGridToggle: ToggleButton? = null



    override fun initialize(location: URL, resources: ResourceBundle) {
        group1!!.toggleGroup = null
        group2!!.toggleGroup = null
        group3!!.toggleGroup = null

        Tooltip.install(normlToggle, Tooltip("Normalize Values"))
        Tooltip.install(actualHToggle, Tooltip("Include Hydrogen Atoms"))

        val converter = object : StringConverter<Double>() {
            private val FORMATTER = NumberFormat.getNumberInstance()

            init {
                FORMATTER.maximumFractionDigits = 2
                FORMATTER.minimumFractionDigits = 0
            }

            override fun toString(`object`: Double?): String {
                return FORMATTER.format(`object`)
            }

            override fun fromString(string: String): Double? {
                return null
            }
        }

        table!!.placeholder = Label("Drag and drop your .pdb files here")
        table.selectionModel.selectionMode = SelectionMode.MULTIPLE

        minColumn!!.cellFactory = TextFieldTableCell.forTableColumn<Model, Double>(converter)
        maxColumn!!.cellFactory = TextFieldTableCell.forTableColumn<Model, Double>(converter)
        avgColumn!!.cellFactory = TextFieldTableCell.forTableColumn<Model, Double>(converter)
        stdColumn!!.cellFactory = TextFieldTableCell.forTableColumn<Model, Double>(converter)
    }

    @FXML private fun handleDragOver(event: DragEvent) {
        if (event.dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY)
        } else {
            event.consume()
        }
    }



    fun selectedItem(): ReadOnlyObjectProperty<Model> {
        return table!!.selectionModel.selectedItemProperty()
    }

    val items: ObservableList<Model>
        get() = table!!.items

    val value: String
        get() {
            var string = ""
            if (actualHToggle!!.isSelected && normlToggle!!.isSelected) {
                string = "HN"
            } else if (actualHToggle.isSelected) {
                string = "H"
            } else if (normlToggle!!.isSelected) {
                string = "N"
            }
            return string
        }

    val group: ObservableList<ToggleButton> get() = group3!!.buttons

    val hToggle: BooleanProperty get() = actualHToggle!!.selectedProperty()
    val nToggle: BooleanProperty get() = normlToggle!!.selectedProperty()
    val hgLines: BooleanProperty get() = hGridToggle!!.selectedProperty()
    val vgLines: BooleanProperty get() = vGridToggle!!.selectedProperty()
    val sToggle: BooleanProperty get() = statsToggle!!.selectedProperty()
    val pToggle: BooleanProperty get() = panelToggle!!.selectedProperty()
    val hSlider: BooleanProperty get() = hSliderToggle!!.selectedProperty()
    val vSlider: BooleanProperty get() = vSliderToggle!!.selectedProperty()
    val pointTg: BooleanProperty get() = pointToggle!!.selectedProperty()
}