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

import com.google.common.collect.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Model;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 7/17/2015.
 */
public class DataTask implements Callable<Model> {

    private final BlockingQueue<String> queue;
    private final String name;

    public DataTask(BlockingQueue<String> queue, String name) {
        this.queue = queue;
        this.name = name;
    }

    @Override
    public Model call() throws Exception {
        Table<String, String, Double> dehydrated = Tables.newCustomTable(Maps.newHashMap(), Maps::newLinkedHashMap);
        Table<String, String, Double> unfiltered = Tables.newCustomTable(Maps.newHashMap(), Maps::newLinkedHashMap);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int pos = Integer.MIN_VALUE;
        while (true) {
            String line = queue.take();
            if (!line.equals("POISON")) {
                String alt = line.substring(16, 17);
                String key = line.substring(21, 22);
                String tmp = line.substring(60, 66).trim();
                String seq = line.substring(23, 26).trim();
                String col = line.substring(12, 20) + seq;
                double val = Double.parseDouble(tmp);
                if (alt.equals(" ") || alt.equals("A")) {
                    unfiltered.put(key, col, val);
                    if (line.charAt(77) != 'H') {
                        dehydrated.put(key, col, val);
                    }
                    int index = Integer.parseInt(seq);
                    if (index != pos) {
                        min = Math.min(min, index);
                        max = Math.max(max, index);
                        pos = index;
                    }
                }
            } else {
                break;
            }
        }
        TableView<List<String>> view = index(min, max, dehydrated);
        return new Model(dehydrated, min, unfiltered, max, view, name);
    }

    private TableView<List<String>> index(int min, int max, Table<String, String, Double> table) {
        ObservableList<List<String>> collection = FXCollections.observableArrayList();
        List<TableColumn<List<String>, String>> columns = Lists.newArrayList();
        columns.add(newColumn(0, "#"));
        int difference = max - min;
        for (int i = 0; i < difference + 1; i++) {
            String index = String.valueOf(min + i);
            collection.add(Lists.newArrayList(index));
        }
        int column = 1;
        for (String row : table.rowKeySet()) {
            int current = Integer.MIN_VALUE;
            for (String key : table.row(row).keySet()) {
                String resName = key.substring(5, 8);
                String resSeq = key.substring(8);
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
            columns.add(newColumn(column, row));
            column++;
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
