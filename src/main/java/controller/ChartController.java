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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.math3.util.Precision;
import org.controlsfx.control.RangeSlider;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Vlad Popa on 7/24/2015.
 */
public class ChartController implements Initializable {

    @FXML private LineChart chart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private RangeSlider xSlider;
    @FXML private RangeSlider ySlider;
    @FXML private VBox chartBox;
    @FXML private HBox chartPane;

    private boolean modifier;
    private Insets insets1;
    private Insets insets2;
    private Insets insets3;
    private Insets insets4;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        insets1 = new Insets(0.0, 43.0, 0.0, 38.0);
        insets2 = new Insets(0.0, 57.0, 0.0, 38.0);
        insets3 = new Insets(0.0, 43.0, 0.0, 40.0);
        insets4 = new Insets(0.0, 57.0, 0.0, 40.0);
        xAxis.setTickLabelFormatter(new NumberStringConverter("#"));
        yAxis.setTickLabelFormatter(new NumberStringConverter("#0.0"));
        xSlider.lowValueProperty().bindBidirectional(xAxis.lowerBoundProperty());
        ySlider.lowValueProperty().bindBidirectional(yAxis.lowerBoundProperty());
        xSlider.highValueProperty().bindBidirectional(xAxis.upperBoundProperty());
        ySlider.highValueProperty().bindBidirectional(yAxis.upperBoundProperty());
        chartBox.getChildren().remove(xSlider);
        chartPane.getChildren().remove(ySlider);
    }

    @FXML
    private void handleScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        if (modifier) {
            if (deltaY > 0) {
                xSlider.incrementLowValue();
                xSlider.decrementHighValue();
            } else {
                xSlider.decrementLowValue();
                xSlider.incrementHighValue();
            }
        } else if (deltaY < 0) {
            if (xAxis.getUpperBound() < xSlider.getMax()) {
                xSlider.incrementLowValue();
                xSlider.incrementHighValue();
            }
        } else if (xAxis.getLowerBound() > xSlider.getMin()) {
            xSlider.decrementLowValue();
            xSlider.decrementHighValue();
        }
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public void setBounds(double[] values) {
        double xTickUnit = Precision.round((0.1 * values[1]), -1);
        double yTickUnit = Precision.round((0.2 * values[3]),  0);
        xSlider.setMin(values[0]);
        xSlider.setMax(values[1]);
        ySlider.setMin(values[2]);
        ySlider.setMax(values[3]);
        xSlider.setLowValue(values[0]);
        ySlider.setLowValue(values[2]);
        xSlider.setHighValue(values[1]);
        ySlider.setHighValue(values[3]);
        xAxis.setTickUnit(xTickUnit);
        yAxis.setTickUnit(yTickUnit);
    }

    public void setModifier(boolean value) {
        modifier = value;
    }
    public void showVSlider(boolean value) {
        if (value) {
            chartPane.getChildren().remove(ySlider);
        } else {
            chartPane.getChildren().add(ySlider);
        }
    }
    public void showHSlider(boolean value) {
        if (value) {
            chartBox.getChildren().remove(xSlider);
        } else {
            chartBox.getChildren().add(xSlider);
        }
    }

    public void setPadding(boolean b, boolean value) {
        if (value && b) {
            xSlider.setPadding(insets2);
        } else if (value) {
            xSlider.setPadding(insets1);
        } else if (b) {
            xSlider.setPadding(insets4);
        } else {
            xSlider.setPadding(insets3);
        }
    }
}
