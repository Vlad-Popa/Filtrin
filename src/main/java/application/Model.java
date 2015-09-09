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

package application;

import com.google.common.collect.*;
import com.google.common.io.Files;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Vlad Popa on 9/1/2015.
 */
public class Model {

    private SimpleStringProperty pdb;
    private SimpleDoubleProperty avg,std, min, max;

    private Path path;
    private Set<String> set;
    private TableView<List<String>> view;
    private Multimap<String, XYChart.Series<Number, Number>> series;
    private Table<String, String, SummaryStatistics> table;

    private int lowerBound, upperBound;

    public static class Builder {

        private Multimap<String, XYChart.Series<Number, Number>> multimap;

        private Table<String, String, SummaryStatistics> table;
        private Table<String, String, Double> heta;
        private StatisticalSummaryValues aggregate;
        private TableView<List<String>> tableView;
        private Path path;
        private String name;
        private int minima, maxima;

        public Builder() {
            table = HashBasedTable.create();
            multimap = TreeMultimap.create(Ordering.natural(), Ordering.usingToString());
        }

        public void setDisplayValues(List<Wrapper> list, String key) {
            for (Wrapper wrapper : list) {
                multimap.putAll(wrapper.getMultimap());
                table.putAll(wrapper.getTable());
            }
            Map<String, SummaryStatistics> map = table.row(key);
            Collection<SummaryStatistics> collection = map.values();
            aggregate = AggregateSummaryStatistics.aggregate(collection);
        }

        public void setFileParams(File file) {
            path = file.toPath();
            name = Files.getNameWithoutExtension(file.toString());
        }
        public void setExtrema(Multimap<String, String> map) {
            String minStr = map.get("MIN").iterator().next();
            String maxStr = map.get("MAX").iterator().next();
            minima = Integer.parseInt(minStr);
            maxima = Integer.parseInt(maxStr);
            if (minima == 1) {
                minima = 0;
            } else if (minima % 10 != 0) {
                double value = 10 + (minima % 10);
                minima -= value;
            }
            if (maxima % 10 != 0) {
                double value = 10 - (maxima % 10);
                maxima += value;
            }
        }
        public void setTableView(TableView<List<String>> tableView) {
            this.tableView = tableView;
        }
        public void setHetatmStats(Table<String, String, Double> heta) {
            this.heta = heta;
        }

        public Model build() {
            return new Model(this);
        }
    }

    private Model(Builder builder) {
        StatisticalSummaryValues aggregate = builder.aggregate;
        lowerBound = builder.minima;
        upperBound = builder.maxima;
        path    = builder.path;
        series  = builder.multimap;
        view    = builder.tableView;
        table   = builder.table;
        set     = Sets.newHashSet(table.columnKeySet());
        pdb     = new SimpleStringProperty(builder.name);
        min     = new SimpleDoubleProperty(aggregate.getMin());
        max     = new SimpleDoubleProperty(aggregate.getMax());
        avg     = new SimpleDoubleProperty(aggregate.getMean());
        std     = new SimpleDoubleProperty(aggregate.getStandardDeviation());

    }

    public Path getPath() {
        return path;
    }
    public String getName() {
        return pdb.get();
    }


    public Collection<XYChart.Series<Number, Number>> getSeries(String key) {
        Collection<XYChart.Series<Number, Number>> collection = series.get(key);
        return Collections2.filter(collection, xy -> set.contains(xy.getName()));
    }

    public double[] getBounds() {
        return new double[]{lowerBound, upperBound, min.get(), max.get()};
    }

    public Set<String> getSet() {
        return Sets.newHashSet(table.columnKeySet());
    }
    public void refreshSet() {
        set = Sets.newHashSet(table.columnKeySet());
    }
    public void updateSet(Boolean newValue, String value) {
        if (newValue) {
            set.add(value);
        } else {
            set.remove(value);
        }
    }

    public TableView<List<String>> getView() {
        return view;
    }

    public void setValues(String key) {
            Map<String, SummaryStatistics> map = Maps.filterKeys(table.row(key), set::contains);
            Collection<SummaryStatistics> collection = map.values();
            StatisticalSummaryValues aggregate = AggregateSummaryStatistics.aggregate(collection);
            min.set(aggregate.getMin());
            max.set(aggregate.getMax());
            avg.set(aggregate.getMean());
            std.set(aggregate.getStandardDeviation());
    }

    public SimpleStringProperty pdbProperty() {
        return pdb;
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
}
