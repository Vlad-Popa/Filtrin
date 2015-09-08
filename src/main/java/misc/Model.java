package misc;

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
    private SimpleDoubleProperty avg = new SimpleDoubleProperty();
    private SimpleDoubleProperty std = new SimpleDoubleProperty();
    private SimpleDoubleProperty min = new SimpleDoubleProperty();
    private SimpleDoubleProperty max = new SimpleDoubleProperty();

    private Path path;
    private Set<String> set;
    private TableView<List<String>> view;
    private Multimap<String, XYChart.Series<Number, Number>> series;
    private Table<String, String, SummaryStatistics> table;

    private int minima, maxima;

    public static class Builder {

        private Multimap<String, XYChart.Series<Number, Number>> multimap;
        private TableView<List<String>> tableView;
        private Table<String, String, SummaryStatistics> table;
        private Table<String, String, Double> heta;
        private Path path;
        private String name;
        private int minima, maxima;

        public Builder() {
            table = HashBasedTable.create();
            multimap = TreeMultimap.create(Ordering.natural(), Ordering.usingToString());
        }

        public void setDisplayValues(List<Wrapper> list) {
            for (Wrapper wrapper : list) {
                multimap.putAll(wrapper.getMultimap());
                table.putAll(wrapper.getTable());
            }
        }
        public void setFileParameters(File file) {
            path = file.toPath();
            name = Files.getNameWithoutExtension(file.toString());
        }
        public void setMaxima(Multimap<String, String> map) {
            String minStr = map.get("MIN").iterator().next();
            String maxStr = map.get("MAX").iterator().next();
            minima = Integer.parseInt(minStr);
            maxima = Integer.parseInt(maxStr);
        }
        public void setView(TableView<List<String>> tableView) {
            this.tableView = tableView;
        }

        public Model build() {
            return new Model(this);
        }

        public void setHeta(Table<String, String, Double> heta) {
            this.heta = heta;
        }
    }

    private Model(Builder builder) {
        minima = builder.minima;
        maxima = builder.maxima;
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

        path = builder.path;
        series = builder.multimap;
        view = builder.tableView;
        table = builder.table;
        set = table.columnKeySet();
        pdb = new SimpleStringProperty(builder.name);
    }

    public Path getPath() {
        return path;
    }
    public Set<String> getSet() {
        return set;
    }
    public Multimap<String, XYChart.Series<Number, Number>> getSeries() {
        return series;
    }

    public double[] getBounds() {
        return new double[]{minima, maxima, min.get(), max.get()};
    }

    public void setValues(String key, Set<String> set) {
        if (table.containsRow(key)) {
            Map<String, SummaryStatistics> map = Maps.filterKeys(table.row(key), set::contains);
            Collection<SummaryStatistics> collection = map.values();
            StatisticalSummaryValues aggregate = AggregateSummaryStatistics.aggregate(collection);
            min.set(aggregate.getMin());
            max.set(aggregate.getMax());
            avg.set(aggregate.getMean());
            std.set(aggregate.getStandardDeviation());
        } else {
            min.set(Double.NaN);
            max.set(Double.NaN);
            avg.set(Double.NaN);
            std.set(Double.NaN);
        }
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
    public TableView<List<String>> getView() {
        return view;
    }
}
