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

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableView
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.controlsfx.control.Notifications
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author Vlad Popa on 9/5/2015.
 */
class TableMenu : ContextMenu() {

    private var tableView: TableView<Model>? = null

    init {
        val remove = MenuItem("Remove")
        val export = MenuItem("Export")
        val normalize = MenuItem("Normalize .pdb")

        this.items.addAll(remove, export, normalize)
        remove.setOnAction { event ->
            val items = tableView!!.selectionModel.selectedItems
            tableView!!.items.removeAll(items)
        }

        val initialDirectory = File(System.getProperty("user.home") + "/Desktop")
        val extensionFilter: FileChooser.ExtensionFilter
        extensionFilter = FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx")
        val fileChooser = FileChooser()
        fileChooser.title = "Export As..."
        fileChooser.initialDirectory = initialDirectory
        fileChooser.initialFileName = "untitled"
        fileChooser.extensionFilters.addAll(extensionFilter)

        export.setOnAction {
            val file = fileChooser.showSaveDialog(Stage())
            if (file != null) {
                try {
                    FileOutputStream(file).use { fos ->
                        val book = XSSFWorkbook()
                        for (model in tableView!!.selectionModel.selectedItems) {
                            val path = model.file
                            val sheet = book.createSheet(model.name)
                            sheet.createRow(0)
                            val key = model.key
                        }
                        book.write(fos)
                        Notifications.create().title("Export Complete").text("The file was successfully written").showConfirm()
                    }
                } catch (e: IOException) {
                    Notifications.create().title("Export Failed").text("The file was not successfully written").showConfirm()
                    e.printStackTrace()
                }
            }
        }
    }

    fun setTableView(tableView: TableView<Model>) {
        this.tableView = tableView
        this.tableView!!.contextMenu = this
    }
}
