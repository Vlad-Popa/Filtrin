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



package misc;

import com.google.common.collect.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class Model {

    private SimpleStringProperty pdb = new SimpleStringProperty();
    private SimpleDoubleProperty num = new SimpleDoubleProperty();
    private SimpleDoubleProperty avg = new SimpleDoubleProperty();
    private SimpleDoubleProperty std = new SimpleDoubleProperty();
    private SimpleDoubleProperty min = new SimpleDoubleProperty();
    private SimpleDoubleProperty max = new SimpleDoubleProperty();
    private SimpleDoubleProperty med = new SimpleDoubleProperty();
    private SimpleDoubleProperty skw = new SimpleDoubleProperty();
    private SimpleDoubleProperty krt = new SimpleDoubleProperty();

    private Path path;
    private Set<String> set;
    private Table<String, String, Double> dehydrated;
    private Table<String, String, Double> unfiltered;
    private Table<String, String, Double> values;
    private TableView<List<String>> view;
    private Multimap<String, XYChart.Series<Number, Number>> series;

    private double lowerBounds;
    private double upperBounds;

    public Model(Table<String, String, Double> dehydrated, int min,
                 Table<String, String, Double> unfiltered, int max,
                 TableView<List<String>> collection, String name) {
        this.series = TreeMultimap.create(Ordering.natural(), Ordering.usingToString());
        this.pdb.set(name);
        this.set = dehydrated.rowKeySet();
        this.view = collection;
        this.values = HashBasedTable.create();
        this.dehydrated = dehydrated;
        this.unfiltered = unfiltered;
        if (min == 1) {
            min = 0;
        } else if (min % 10 != 0) {
            double value = 10 + (min % 10);
            min -= value;
        }
        if (max % 10 != 0) {
            double value = 10 - (max % 10);
            max += value;
        }
        this.lowerBounds = min;
        this.upperBounds = max;
    }

    public boolean containsKey(String key) {
        return series.containsKey(key);
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public double[] getBounds(String key) {
        double min = values.get(key, "min");
        double max = values.get(key, "max");
        min -= Math.abs(0.2 * min);
        max += Math.abs(0.2 * max);
        return new double[]{lowerBounds, upperBounds, min, max};
    }

    public void putAllSeries(Multimap<String, XYChart.Series<Number, Number>> multimap) {
        series.putAll(multimap);
    }

    public Multimap<String, XYChart.Series<Number, Number>> getSeries() {
        return series;
    }

    public Table<String, String, Double> getTable(boolean value) {
        if (value) {
            return unfiltered;
        } else {
            return dehydrated;
        }
    }

    public void putValues(String key, StatisticalSummary stats) {
        int n = (int) stats.getN();
        if (key.startsWith("Main chain")) n *= 4;
        if (key.startsWith("Backbone")) n *= 3;
        if (key.startsWith("All atoms")) {
            if (!key.contains("H")) {
                n = dehydrated.size();
            } else {
                n = unfiltered.size();
            }
        }
        if (key.startsWith("Side chain")) {
            if (!key.contains("H")) {
                n = (dehydrated.size()) - (n * 4);
            } else {
                n = (unfiltered.size()) - (n * 4);
            }
        }
        values.put(key, "num", (double) n);
        values.put(key, "min", stats.getMin());
        values.put(key, "max", stats.getMax());
        values.put(key, "avg", stats.getMean());
        values.put(key, "std", stats.getStandardDeviation());
    }

    public void setValues(String key) {
        num.set(values.get(key, "num"));
        min.set(values.get(key, "min"));
        max.set(values.get(key, "max"));
        avg.set(values.get(key, "avg"));
        std.set(values.get(key, "std"));
    }

    public Set<String> getSet() {
        return set;
    }

    public SimpleStringProperty pdbProperty() {
        return pdb;
    }
    public SimpleDoubleProperty numProperty() {
        return num;
    }
    public SimpleDoubleProperty avgProperty() {
        return avg;
    }
    public SimpleDoubleProperty stdProperty() {
        return std;
    }
    public SimpleDoubleProperty minProperty() {
        return min;
    }
    public SimpleDoubleProperty maxProperty() {
        return max;
    }
    public SimpleDoubleProperty medProperty() {
        return med;
    }
    public SimpleDoubleProperty skwProperty() {
        return skw;
    }
    public SimpleDoubleProperty krtProperty() {
        return krt;
    }
    public TableView<List<String>> getView() {
        return view;
    }
}