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

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import java.net.URL
import java.util.*

/**
 * @author Vlad Popa on 8/6/2015.
 */
class MenuController : Initializable {

    @FXML private val group: ToggleGroup? = null
    @FXML val stats: MenuItem? = null

    private var alert: Alert? = null
    private var about: Alert? = null
    private var data: Alert? = null
    private var exporting: Alert? = null

    override fun initialize(location: URL, resources: ResourceBundle) {
        alert = Alert(Alert.AlertType.INFORMATION)
        alert!!.title = "Licensing"
        alert!!.headerText = "This software uses the following libraries:"
        alert!!.contentText = "Google Guava, Copyright (C) 2011, The Guava Authors\nApache Commons Math and Apache POI,\nCopyright (C) 2001-2015, The Apache Software Foundation\nControlsFX, Copyright (C) 2013-2015, The ControlsFX Authors"
        about = Alert(Alert.AlertType.INFORMATION)
        about!!.title = "About"
        about!!.headerText = "Copyright (C) 2015 Vlad Popa"
        about!!.contentText = "Filtrin is a statistics and filtering application designed to analyze, display, and export temperature factor (Beta-factor) values extracted from Protein Data Bank (.pdb) files."
        data = Alert(Alert.AlertType.INFORMATION)
        data!!.headerText = "How data is calculated"
        data!!.contentText = "Filtrin works by first reading and collecting relevant information from each line of the .pdb file, including: temperature factor values, atom names, residue names, residue sequence numbers, and chain Ids. Afterwards, Filtrin sorts the data according to the chains present, and then into subcategories such as: Residue atoms, Main chain atoms, Backbone atoms, C-Alpha atoms, and Side chain atoms. Note: By default, Filtrin will filter out all hydrogen atoms from the data pool. If you wish to include hydrogen atoms in the calculations, simply toggle the \"Include Hydrogen Atoms\" button.\n\nOnce the values have been sorted and filtered, Filtrin will compute temperature factor averages on a per residue basis, while also taking the different categories into consideration. This means that if the category is \"Main Chain\", for instance, then only the values from the N, CA, C and O atoms will be averaged, and it is that number that will used for further calculations, including generating series for the chart, and statistics for the table. Note: the \"Normalize Values\" toggle button is selected by default, and normalization is based on the averages (i.e.: not the individual temperature factor values). If you wish to normalize values, on a per chain and per atom basis, then you may utilize Filtrin's export functionality."
        exporting = Alert(Alert.AlertType.INFORMATION)
        exporting!!.headerText = "Exporting options"
        exporting!!.contentText = "Filtrin has the ability to export temperature factor data to a Microsoft Excel spreadsheet (.xlsx) in a per chain arrangement (i.e.: chains are separated into sections, which are composed of multiple vertical columns). To export, simply right click on a file in the statistics table, choose the export option, and specify the directory the spreadsheet file will be saved to. Note: Filtrin will export data depending on what category is selected. Given that, Filtrin will draw from the master collection pool and reapply filters to match the settings (again, on a per chain basis). The normalized values that are present in the spreadsheet are calculated from the individual atoms, which may give different results on than what is seen on the chart, which are averages."
    }

    @FXML
    private fun handleLicenceDialogue() {
        alert!!.showAndWait()
    }

    @FXML
    private fun handleAboutDialogue() {
        about!!.showAndWait()
    }

    @FXML
    private fun handleNormalDialogue() {
        data!!.showAndWait()
    }

    @FXML
    private fun handleExportDialog() {
        exporting!!.showAndWait()
    }


    fun selectedItem(): ReadOnlyObjectProperty<Toggle> {
        return group!!.selectedToggleProperty()
    }

    val value: String
        get() {
            val item = group!!.selectedToggle as RadioMenuItem
            return item.text
        }
}
