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

package task;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 9/5/2015.
 */
public class IndexTask implements Callable<TableView<List<String>>> {

    private Multimap<String, String> multimap;

    public IndexTask(Multimap<String, String> multimap) {
        this.multimap = multimap;
    }

    @Override
    public TableView call() throws Exception {
        ObservableList<List<String>> collection = FXCollections.observableArrayList();
        List<TableColumn<List<String>, String>> columns = Lists.newArrayList();
        columns.add(newColumn(0, "#"));

        String minStr = multimap.get("MIN").iterator().next();
        String maxStr = multimap.get("MAX").iterator().next();
        int min = Integer.parseInt(minStr);
        int max = Integer.parseInt(maxStr);

        int difference = max - min;
        for (int i = 0; i < difference + 1; i++) {
            String index = String.valueOf(min + i);
            collection.add(Lists.newArrayList(index));
        }

        int column = 1;
        for (String key : multimap.keySet()) {
            if (key.length() == 1) {
                int current = Integer.MIN_VALUE;
                for (String value : multimap.get(key)) {
                    String resName = value.substring(5, 8);
                    String resSeq = value.substring(11, 14).trim();
                    int index = Integer.parseInt(resSeq);
                    int position = index - min;
                    if (current != position) {
                        List<String> list = collection.get(position);
                        int size = list.size();
                        if (list.size() < column) {
                            for (int i = size; i < column; i++) {
                                list.add(" ");
                            }
                        }
                        list.add(resName);
                        current = position;
                    }
                }
                columns.add(newColumn(column, key));
                column++;
            }
        }

        TableView<List<String>> view = new TableView<>();
        view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        view.setEditable(false);
        view.getColumns().addAll(columns);
        view.getItems().addAll(collection);
        VBox.setVgrow(view, Priority.ALWAYS);
        return view;
    }

    private TableColumn<List<String>, String> newColumn(int index, String title) {
        TableColumn<List<String>, String> column = new TableColumn<>(title);
        column.setCellValueFactory(p -> {
            if (index < p.getValue().size()) {
                return new SimpleStringProperty((p.getValue().get(index)));
            } else {
                return null;
            }
        });
        column.setSortable(false);
        return column;
    }
}
