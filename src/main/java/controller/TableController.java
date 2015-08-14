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

import com.google.common.collect.*;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.StringConverter;
import model.Model;
import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.controlsfx.control.SegmentedButton;
import service.Service;
import task.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    @FXML private TableColumn<Model, Double> numColumn;
    @FXML private TableColumn<Model, Double> minColumn;
    @FXML private TableColumn<Model, Double> maxColumn;
    @FXML private TableColumn<Model, Double> avgColumn;
    @FXML private TableColumn<Model, Double> stdColumn;

    @FXML private SegmentedButton group2;
    @FXML private SegmentedButton group1;
    @FXML private SegmentedButton toggleGroup;
    @FXML private ToggleButton hSliderToggle;
    @FXML private ToggleButton vSliderToggle;
    @FXML private ToggleButton panelToggle;
    @FXML private ToggleButton hydroToggle;
    @FXML private ToggleButton normlToggle;
    @FXML private ToggleButton statsToggle;
    @FXML private ToggleButton hGridToggle;
    @FXML private ToggleButton vGridToggle;


    private MenuItem export;

    private ObservableList<String> files = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        group1.setToggleGroup(null);
        group2.setToggleGroup(null);
        toggleGroup.setToggleGroup(null);

        Tooltip.install(normlToggle, new Tooltip("Normalize Values"));
        Tooltip.install(hydroToggle, new Tooltip("Include Hydrogen Atoms"));

        table.setPlaceholder(new Label("Drag and drop your .pdb files here"));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem remove = new MenuItem("Remove");
        export = new MenuItem("Export");
        MenuItem normalize = new MenuItem("Normalize .pdb");
        normalize.setOnAction(event -> {
            BlockingQueue<String> queue = new ArrayBlockingQueue<>(200);
            for (Model model : table.getSelectionModel().getSelectedItems()) {
                Path path = model.getPath();
                Service.INSTANCE.execute(new ExtractTask(queue, path));
                Service.INSTANCE.execute(new RewriteTask(queue, path.toString()));
            }
        });
        contextMenu.getItems().addAll(remove, export, normalize);
        remove.setOnAction(event -> {
            ObservableList<Model> items = table.getSelectionModel().getSelectedItems();
            for (Model model : items) {
                String name = model.pdbProperty().get();
                files.remove(name);
            }
            table.getItems().removeAll(items);
        });
        table.setContextMenu(contextMenu);


        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pdbColumn.setCellValueFactory(new PropertyValueFactory<>("pdb"));
        numColumn.setCellValueFactory(new PropertyValueFactory<>("num"));
        minColumn.setCellValueFactory(new PropertyValueFactory<>("min"));
        maxColumn.setCellValueFactory(new PropertyValueFactory<>("max"));
        avgColumn.setCellValueFactory(new PropertyValueFactory<>("avg"));
        stdColumn.setCellValueFactory(new PropertyValueFactory<>("std"));

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

        numColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        minColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        maxColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        avgColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        stdColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
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
            success = true;
            List<Model> items = Lists.newArrayList();
            for (File file : dragboard.getFiles()) {
                String name = file.getName();
                int index = name.lastIndexOf(".");
                String ext = name.substring(index);
                if (!files.contains(name) && ext.equals(".pdb")) {
                    files.add(name);
                    BlockingQueue<String> queue = new ArrayBlockingQueue<>(200);
                    BlockingQueue<String> hetatm = new ArrayBlockingQueue<>(200);
                    ListenableFuture<Model> future;
                    ListenableFuture<StatisticalSummary> futureStats;

                    Service.INSTANCE.execute(new FileTask(queue, hetatm, file));
                    future = Service.INSTANCE.submit(new DataTask(queue, name));
                    futureStats = Service.INSTANCE.submit(new HetTask(hetatm));
                    try {
                        Model model = future.get();
                        model.setPath(file.toPath());
                        model.putValues("Hetero atoms", futureStats.get());
                        callback(model);
                        items.add(model);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!items.isEmpty()) {
                int size = items.size();
                Model model = items.get(size - 1);
                table.getItems().addAll(items);
                table.getSelectionModel().select(model);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    public void callback(Model model) {
        Multimap<String, XYChart.Series<Number, Number>> series = Multimaps.synchronizedMultimap(HashMultimap.create());
        Multimap<String, SummaryStatistics> bounds = Multimaps.synchronizedMultimap(HashMultimap.create());

        List<SeriesTask> list = Lists.newArrayList();
        Table<String, String, Double> data = model.getTable(hydroToggle.isSelected());
        String condition = getValue();
        for (String key : data.rowKeySet()) {
            Map<String, Double> map = data.row(key);
            list.add(new SeriesTask(condition, key, map, series, bounds));
        }

        try {
            Service.INSTANCE.invokeAll(list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        model.putAllSeries(series);
        for (String key : bounds.keySet()) {
            Collection<SummaryStatistics> stats = bounds.get(key);
            model.putValues(key, AggregateSummaryStatistics.aggregate(stats));
        }
    }

    public ReadOnlyObjectProperty<Model> selectedItem() {
        return table.getSelectionModel().selectedItemProperty();
    }

    public ObservableList<Model> getSelectedItems() {
        return table.getSelectionModel().getSelectedItems();
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
            string = " ";
        }
        return string;
    }

    public MenuItem getExport() {
        return export;
    }

    public ObservableList<ToggleButton> getGroup() {
        return toggleGroup.getButtons();
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
}