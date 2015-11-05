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

import application.HetModel;
import application.Model;
import com.google.common.collect.Lists;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
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

    private ObservableList<ToggleButton> buttons;
    private ObservableList<XYChart.Series<Number, Number>> series;
    private boolean shift;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.addEventHandler(KeyEvent.ANY, event -> {
            chartController.setModifier(event.isControlDown());
            shift = event.isShiftDown();
        });

        series = FXCollections.observableArrayList();
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
        final NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(0);
        menuController.getStats().setOnAction(event -> {
            try {
                TableView<HetModel> view = FXMLLoader.load(getClass().getResource("/hetatm.fxml"));
                Model model = tableController.selectedItem().get();
                Map<String, double[]> map = model.getHetaMap();
                for (String key : map.keySet()) {
                    double[] array = map.get(key);
                    DescriptiveStatistics stats = new DescriptiveStatistics(array);
                    double min = stats.getMin();
                    double max = stats.getMax();
                    double avg = stats.getMean();
                    double std = stats.getStandardDeviation();
                    HetModel het = new HetModel(key,
                            formatter.format(min),
                            formatter.format(max),
                            formatter.format(avg),
                            formatter.format(std));
                    view.getItems().add(het);
                }
                Scene scene = new Scene(view);
                scene.getStylesheets().add("/stylesheet.css");
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        tableController.selectedItem().addListener(modelListener());
        tableController.getHToggle().addListener(statisticsListener());
        tableController.getNToggle().addListener(statisticsListener());
        tableController.getHGLines().addListener(hGridListener());
        tableController.getVGLines().addListener(vGridListener());
        tableController.getPToggle().addListener(dividerListener1(divider1, 0.8, 1.0));
        tableController.getSToggle().addListener(dividerListener2(divider2, 0.5, 1.0));
        tableController.getHSlider().addListener(hSlideListener());
        tableController.getVSlider().addListener(vSlideListener());
        tableController.getPointTg().addListener(pointListener());
    }

    private ChangeListener<Toggle> categoryListener() {
        return ((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String value = menuController.getValue();
                switch (value) {
                    case "Main chain":
                    case "Backbone":
                    case "C-Alpha":
                        tableController.getHToggle().set(false);
                        tableController.getActualHToggle().setDisable(true);
                        break;
                    default:
                        tableController.getActualHToggle().setDisable(false);
                        break;
                }
                if (!tableController.getItems().isEmpty()) {
                    Platform.runLater(() -> {
                        Model model = tableController.selectedItem().get();
                        String key = value + tableController.getValue();
                        for (Model item : tableController.getItems()) {
                            item.setValues(key);
                        }
                        series.setAll(model.getSeries(key));
                        chartController.setBounds(model.getBounds());
                    });
                }
            }
        });
    }

    private ChangeListener<Model> modelListener() {
        return ((observable, oldValue, newValue) -> {
            String value = menuController.getValue();
            if (!shift && !tableController.getItems().isEmpty()) {
                newValue.refreshSet();
                String key = value + tableController.getValue();
                List<ToggleButton> toggleList = getButtons(newValue);
                Platform.runLater(() -> {
                    series.setAll(newValue.getSeries(key));
                    chartController.setBounds(newValue.getBounds());
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
    private ChangeListener<Boolean> pointListener() {
        return ((observable, oldValue, newValue) -> chartController.getChart().setCreateSymbols(newValue));
    }

    private ChangeListener<Boolean> statisticsListener() {
        return (observable, oldValue, newValue) -> {
            if (!tableController.getItems().isEmpty()) {
                String key = menuController.getValue() + tableController.getValue();
                for (Model item : tableController.getItems()) {
                    item.setValues(key);
                }
                Model model = tableController.selectedItem().get();
                series.setAll(model.getSeries(key));
                chartController.setBounds(model.getBounds());
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
                model.updateSet(newValue, string);
                String key = menuController.getValue() + tableController.getValue();
                series.setAll(model.getSeries(key));
                model.setValues(key);
            });
            list.add(button);
        }
        return list;
    }
}

