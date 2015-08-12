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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Model;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.controlsfx.control.Notifications;
import service.Service;
import task.WriteTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Vlad Popa on 7/18/2015.
 */
public class RootController implements Initializable {

    @FXML private VBox root;
    @FXML private VBox table;
    @FXML private VBox vBox;
    @FXML private VBox chart;
    @FXML private MenuBar menu;
    @FXML private SplitPane outerPane;
    @FXML private SplitPane graphPane;
    @FXML private MenuController menuController;
    @FXML private ChartController chartController;
    @FXML private TableController tableController;

    private ObservableList<String> chains;
    private ObservableList<ToggleButton> buttons;
    private ObservableList<XYChart.Series<Number, Number>> series;
    private Predicate<XYChart.Series<Number, Number>> predicate = xy -> chains.contains(xy.getName());

    private boolean shift;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.addEventHandler(KeyEvent.ANY, event -> {
            chartController.setModifier(event.isControlDown());
            shift = event.isShiftDown();
        });

        series = FXCollections.observableArrayList();
        chains = FXCollections.observableArrayList();
        buttons = FXCollections.observableArrayList();

        Bindings.bindContentBidirectional(series, chartController.getChart().getData());
        Bindings.bindContentBidirectional(buttons, tableController.getGroup());
        DoubleProperty divider1 = outerPane.getDividers().get(0).positionProperty();
        DoubleProperty divider2 = graphPane.getDividers().get(0).positionProperty();

        root.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (!tableController.getPToggle().get()) {
                Platform.runLater(() -> divider1.set(1.0));
            } else {
                Platform.runLater(() -> divider1.set(0.8));
            }
            if (tableController.getSToggle().get()) {
                Platform.runLater(() -> divider2.set(0.5));
            } else {
                Platform.runLater(() -> divider2.set(1.0));
            }
        });

        menuController.selectedItem().addListener(categoryListener());
        tableController.getItems().addListener((ListChangeListener<Model>) c -> {
            while (c.next()) {
                for (Model model : c.getAddedSubList()) {
                    model.setValues(menuController.getValue() + tableController.getValue());
                }
            }
        });

        tableController.getExport().setOnAction(exportAction());
        tableController.selectedItem().addListener(modelListener());
        tableController.getHToggle().addListener(statisticsListener());
        tableController.getNToggle().addListener(statisticsListener());
        tableController.getHGLines().addListener(hGridListener());
        tableController.getVGLines().addListener(vGridListener());
        tableController.getPToggle().addListener(dividerListener1(divider1, 0.8, 1.0));
        tableController.getSToggle().addListener(dividerListener2(divider2, 0.5, 1.0));
        tableController.getHSlider().addListener(hSlideListener());
        tableController.getVSlider().addListener(vSlideListener());
    }

    private EventHandler<ActionEvent> exportAction() {
        return event -> {
            File file = menuController.getFileChooser().showSaveDialog(new Stage());
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    Workbook book = new XSSFWorkbook();
                    List<WriteTask> tasks = Lists.newArrayList();
                    for (Model model : tableController.getSelectedItems()) {
                        Sheet sheet = book.createSheet(model.pdbProperty().get());
                        Table<String, String, Double> table = model.getTable(tableController.getHToggle().get());
                        tasks.add(new WriteTask(table, sheet, menuController.getValue()));
                    }
                    Service.INSTANCE.invokeAll(tasks);
                    book.write(fos);
                    Notifications.create().title("Export Complete").text("The file was successfully written").showConfirm();
                } catch (IOException | InterruptedException e) {
                    Notifications.create().title("Export Failed").text("The file was not successfully written").showConfirm();
                    e.printStackTrace();
                }
            }
        };
    }

    private ChangeListener<Toggle> categoryListener() {
        return ((observable, oldValue, newValue) -> {
            if (!tableController.getItems().isEmpty() && newValue != null) {
                Model model = tableController.selectedItem().get();
                String key = menuController.getValue() + tableController.getValue();
                Platform.runLater(() -> {
                    if (model.containsKey(key)) {
                        for (Model item : tableController.getItems()) {
                            item.setValues(key);
                        }
                        Multimap<String, XYChart.Series<Number, Number>> filter;
                        filter = Multimaps.filterValues(model.getSeries(), predicate);
                        series.setAll(filter.get(key));
                        chartController.setBounds(model.getBounds(key));
                    } else if (menuController.getValue().equals("Hetero atoms")) {
                        for (Model item : tableController.getItems()) {
                            item.setValues("Hetero atoms");
                        }
                        series.clear();
                    }
                });
            }
        });
    }

    private ChangeListener<Model> modelListener() {
        return ((observable, oldValue, newValue) -> {
            String value = menuController.getValue();
            if (!value.equals("Hetero atoms") && !shift && !tableController.getItems().isEmpty()) {
                chains.setAll(newValue.getSet());
                String key = value + tableController.getValue();
                if (!newValue.containsKey(key)) {
                    tableController.callback(newValue);
                }
                List<ToggleButton> toggleList = getButtons(newValue);
                Platform.runLater(() -> {
                    series.setAll(newValue.getSeries().get(key));
                    chartController.setBounds(newValue.getBounds(key));
                    if (tableController.getPToggle().get()) {
                        vBox.getChildren().setAll(newValue.getView());
                    }
                    buttons.setAll(toggleList);
                });
            } else if (tableController.getItems().isEmpty()) {
                series.clear();
                buttons.clear();
            }
        });
    }

    private ChangeListener<Boolean> vSlideListener() {
        return ((observable, oldValue, newValue) -> {
            chartController.showVSlider(oldValue);
            chartController.setPadding(newValue, tableController.getNToggle().get());
        });
    }

    private ChangeListener<Boolean> hSlideListener() {
        return ((observable, oldValue, newValue) -> chartController.showHSlider(oldValue));
    }

    private ChangeListener<Boolean> vGridListener() {
        return ((observable, oldValue, newValue) -> {
            chartController.getChart().setVerticalGridLinesVisible(newValue);
            chartController.getChart().setVerticalZeroLineVisible(newValue);
        });
    }

    private ChangeListener<Boolean> hGridListener() {
        return ((observable, oldValue, newValue) -> {
            chartController.getChart().setHorizontalGridLinesVisible(newValue);
            chartController.getChart().setHorizontalZeroLineVisible(newValue);
        });
    }

    private ChangeListener<Boolean> statisticsListener() {
        return (observable, oldValue, newValue) -> {
            if (!tableController.getItems().isEmpty()) {
                String key = menuController.getValue() + tableController.getValue();
                for (Model item : tableController.getItems()) {
                    if (!item.containsKey(key)) {
                        tableController.callback(item);
                    }
                    item.setValues(key);
                }
                Model model = tableController.selectedItem().get();
                Multimap<String, XYChart.Series<Number, Number>> filter;
                filter = Multimaps.filterValues(model.getSeries(), predicate);
                series.setAll(filter.get(key));
                chartController.setBounds(model.getBounds(key));
                boolean toggle = tableController.getNToggle().get();
                chartController.setPadding(tableController.getVSlider().get(), toggle);
            }
        };
    }

    private ChangeListener<Boolean> dividerListener1(DoubleProperty divider, double to, double from) {
        return (observable, oldValue, newValue) -> {
            KeyValue value;
            if (newValue && !tableController.getItems().isEmpty()) {
                value = new KeyValue(divider, to);
                Model model = tableController.selectedItem().get();
                vBox.getChildren().setAll(model.getView());
            } else {
                value = new KeyValue(divider, from);
                vBox.getChildren().clear();
            }
            new Timeline(new KeyFrame(Duration.seconds(0.2), value)).play();
        };
    }

    private ChangeListener<Boolean> dividerListener2(DoubleProperty divider, double to, double from) {
        return (observable, oldValue, newValue) -> {
            KeyValue value = newValue ?
                    new KeyValue(divider, to) :
                    new KeyValue(divider, from);
            new Timeline(new KeyFrame(Duration.seconds(0.2), value)).play();
        };
    }

    private List<ToggleButton> getButtons(Model model) {
        List<ToggleButton> list = Lists.newArrayList();
        for (String string : model.getSet()) {
            ToggleButton button = new ToggleButton(string);
            button.setPrefSize(30, 30);
            button.setSelected(true);
            button.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !chains.contains(string)) {
                    chains.add(string);
                } else {
                    chains.remove(string);
                }
                String key = menuController.getValue() + tableController.getValue();
                Multimap<String, XYChart.Series<Number, Number>> filter;
                filter = Multimaps.filterValues(model.getSeries(), predicate);
                series.setAll(filter.get(key));
            });
            list.add(button);
        }
        return list;
    }
}

