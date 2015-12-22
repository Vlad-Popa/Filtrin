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

import com.google.common.collect.Lists
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch

/**
 * @author Vlad Popa on 9/5/2015.
 */
class ListTask(private val queue: BlockingQueue<String>,
               private val latch: CountDownLatch) : Runnable {

    private val collection = FXCollections.observableArrayList<ArrayList<String>>()
    private val columns = Lists.newArrayList<TableColumn<List<String>, String>>()

    override fun run() {
        columns.add(newColumn(0, "#"))
        var min = 0
        var column = 0
        var chainId = ' '
        var temporary = Integer.MIN_VALUE
        while (true) {
            val line = queue.take()
            if (line != "POISON") {
                val resName  = line.substring(17, 20)
                val resSeq   = line.substring(23, 26).toInt()
                val lineId   = line[21]
                val position = resSeq - min
                if (chainId == ' ') {
                    chainId = lineId
                    min = resSeq
                } else if (chainId != lineId) {
                    columns.add(newColumn(column, chainId.toString()))
                    chainId = lineId
                    column++
                }
                if (temporary != position) {
                    if (position >= collection.size) {
                        collection.add(arrayListOf(resSeq.toString()))
                    }
                    val list = collection[position]
                    if (list.size < column) {
                        for (i in list.size..column - 1) {
                            list.add(" ")
                        }
                    }
                    list.add(resName)
                    temporary = position
                }
            } else break
        }
        latch.countDown()
    }

    private fun newColumn(index: Int, title: String): TableColumn<List<String>, String>? {
        val column = TableColumn<List<String>, String>(title)
        column.setCellValueFactory { return@setCellValueFactory SimpleStringProperty(it.value[index]) }
        column.isSortable = false
        return column
    }

    private fun setUpTable() {
        val view = TableView<List<String>>()
        view.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        view.isEditable = false
        view.columns.addAll(columns)
        view.items.addAll(collection)
        VBox.setVgrow(view, Priority.ALWAYS)
    }
}