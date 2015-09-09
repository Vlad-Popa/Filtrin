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

package controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.StringConverter;
import org.controlsfx.control.SegmentedButton;
import application.Model;
import application.TableMenu;
import application.Wrapper;
import application.Service;
import task.*;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * @author Vlad Popa on 7/25/2015.
 */
public class TableController implements Initializable {

    @FXML private TableView<Model> table;
    @FXML private TableColumn<Model, String> pdbColumn;
    @FXML private TableColumn<Model, Double> minColumn;
    @FXML private TableColumn<Model, Double> maxColumn;
    @FXML private TableColumn<Model, Double> avgColumn;
    @FXML private TableColumn<Model, Double> stdColumn;

    @FXML private SegmentedButton group2;
    @FXML private SegmentedButton group1;
    @FXML private SegmentedButton group3;
    @FXML private ToggleButton hSliderToggle;
    @FXML private ToggleButton vSliderToggle;
    @FXML private ToggleButton pointToggle;
    @FXML private ToggleButton panelToggle;
    @FXML private ToggleButton hydroToggle;
    @FXML private ToggleButton normlToggle;
    @FXML private ToggleButton statsToggle;
    @FXML private ToggleButton hGridToggle;
    @FXML private ToggleButton vGridToggle;

    private FileNameExtensionFilter filter = new FileNameExtensionFilter("PDB", "pdb");
    private AsyncFunction<Multimap<String, String>, TableView<List<String>>> function1;
    private AsyncFunction<Multimap<String, String>, Table<String, String, Double>> function2;
    private AsyncFunction<Multimap<String, String>, List<Multimap<String, Double>>> function3;
    private AsyncFunction<List<Multimap<String, Double>>, List<Wrapper>> function4;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        group1.setToggleGroup(null);
        group2.setToggleGroup(null);
        group3.setToggleGroup(null);

        Tooltip.install(normlToggle, new Tooltip("Normalize Values"));
        Tooltip.install(hydroToggle, new Tooltip("Include Hydrogen Atoms"));

        TableMenu menu = new TableMenu();
        menu.setTableView(table);

        table.setPlaceholder(new Label("Drag and drop your .pdb files here"));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        StringConverter<Double> converter = new StringConverter<Double>() {
            private final NumberFormat formatter = NumberFormat.getNumberInstance();
            {
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(0);
            }
            @Override
            public String toString(Double object) {
                return formatter.format(object);
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        };

        minColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        maxColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        avgColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        stdColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));

        function1 = multimap -> Service.INSTANCE.submit(new IndexTask(multimap));
        function2 = multimap -> Service.INSTANCE.submit(new HetatmTask(multimap));
        function3 = multimap -> {
            List<ListenableFuture<Multimap<String, Double>>> tasks = Lists.newArrayList();
            for (String key : multimap.keySet()) {
                if (key.length() == 1) {
                    Collection<String> collection = multimap.get(key);
                    tasks.add(Service.INSTANCE.submit(new SortTask(collection, key)));
                }
            }
            return Futures.allAsList(tasks);
        };
        function4 = list -> {
            List<ListenableFuture<Wrapper>> wrappers = Lists.newArrayList();
            for (Multimap<String, Double> multimap : list) {
                wrappers.add(Service.INSTANCE.submit(new SeriesTask(multimap)));
            }
            return Futures.allAsList(wrappers);
        };
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        } else {
            event.consume();
        }
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;
        if (dragboard.hasFiles()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            try {
                MenuBar menu = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MenuController controller = loader.<MenuController>getController();
            String value = controller.getValue() + this.getValue();
            success = true;

            dragboard.getFiles().stream().filter(filter::accept).forEach(file -> {
                Path path = file.toPath();
                BlockingQueue<String> queue = new ArrayBlockingQueue<>(200);
                Service.INSTANCE.execute(new FileTask(queue, path));
                ListenableFuture<Multimap<String, String>> future1 = Service.INSTANCE.submit(new DataTask(queue));
                ListenableFuture<TableView<List<String>>> future2 = Futures.transform(future1, function1);
                ListenableFuture<Table<String, String, Double>> future3 = Futures.transform(future1, function2);
                ListenableFuture<List<Multimap<String, Double>>> future4 = Futures.transform(future1, function3);
                ListenableFuture<List<Wrapper>> future5 = Futures.transform(future4, function4);
                try {
                    Model.Builder model = new Model.Builder();
                    model.setDisplayValues(future5.get(), value);
                    model.setExtrema(future1.get());
                    model.setTableView(future2.get());
                    model.setHetatmStats(future3.get());
                    model.setFileParams(file);
                    table.getItems().add(model.build());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            int size = table.getItems().size();
            table.getSelectionModel().select(size - 1);
        }
        event.setDropCompleted(success);
        event.consume();
    }

    public ReadOnlyObjectProperty<Model> selectedItem() {
        return table.getSelectionModel().selectedItemProperty();
    }

    public ObservableList<Model> getItems() {
        return table.getItems();
    }

    public String getValue() {
        String string;
        if (hydroToggle.isSelected() && normlToggle.isSelected()) {
            string = "HN";
        } else if (hydroToggle.isSelected()) {
            string = "H";
        } else if (normlToggle.isSelected()) {
            string = "N";
        } else {
            string = "";
        }
        return string;
    }

    public ObservableList<ToggleButton> getGroup() {
        return group3.getButtons();
    }
    public ToggleButton getActualHToggle() {
        return hydroToggle;
    }

    public BooleanProperty getHToggle() {
        return hydroToggle.selectedProperty();
    }
    public BooleanProperty getNToggle() {
        return normlToggle.selectedProperty();
    }
    public BooleanProperty getHGLines() {
        return hGridToggle.selectedProperty();
    }
    public BooleanProperty getVGLines() {
        return vGridToggle.selectedProperty();
    }
    public BooleanProperty getSToggle() {
        return statsToggle.selectedProperty();
    }
    public BooleanProperty getPToggle() {
        return panelToggle.selectedProperty();
    }
    public BooleanProperty getHSlider() {
        return hSliderToggle.selectedProperty();
    }
    public BooleanProperty getVSlider() {
        return vSliderToggle.selectedProperty();
    }
    public BooleanProperty getPointTg() {
        return pointToggle.selectedProperty();
    }
}