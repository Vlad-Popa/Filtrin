package task;

import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import rewrite.Wrapper;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 9/2/2015.
 */
public class SeriesTask implements Callable<Wrapper> {

    private Collection<String> collection;
    private String chainId;

    public SeriesTask(Collection<String> collection, String chainId) {
        this.collection = collection;
        this.chainId = chainId;
    }

    @Override
    public Wrapper call() throws Exception {
        Multimap<String, Double> multimap = sortValues();
        String str = Iterables.get(collection, 0);
        String idx = str.substring(11, 15).trim();
        int resSeq = Integer.parseInt(idx);

        Multimap<String, XYChart.Series> series = HashMultimap.create();
        Table<String, String, SummaryStatistics> stats = HashBasedTable.create();
        for (String key : multimap.keySet()) {
            double[] doubles = Doubles.toArray(multimap.get(key));
            double[] normals = StatUtils.normalize(doubles);
            SummaryStatistics statistics1 = new SummaryStatistics();
            SummaryStatistics statistics2 = new SummaryStatistics();
            ObservableList<XYChart.Data<Number, Number>> data1 = getData(doubles, resSeq, statistics1);
            ObservableList<XYChart.Data<Number, Number>> data2 = getData(normals, resSeq, statistics2);
            series.put(key      , new XYChart.Series<>(chainId, data1));
            series.put(key + "N", new XYChart.Series<>(chainId, data2));
            stats.put(key, chainId, statistics1);
            stats.put(key + "N", chainId, statistics2);
        }
        return new Wrapper();
    }

    private Multimap<String, Double> sortValues() {
        ImmutableMultimap.Builder<String, Double> builder = ImmutableMultimap.builder();
        double sum = 0;
        double tmp = 0;
        double hyd = 0;
        int n = 0;
        int i = 0;
        for (String line : collection) {
            String atm = line.substring(0, 4);
            String fac = line.substring(47, 53);
            double val = Double.parseDouble(fac);
            sum += val;
            if (atm.contains("H")) {
                hyd += val;
                i++;
            } else if (atm.equals(" N  ") && n != 0) {
                double num = sum - val;
                double rem = (num - tmp) / (n - 4);
                double re2 = (num - tmp) / (n - 4 - i);
                if (!Doubles.isFinite(rem)) rem = re2 = 0;
                builder.put("All atomsH", num / n);
                builder.put("All atoms", (num - hyd) / (n - i));
                builder.put("Side chainH", rem);
                builder.put("Side chain", re2);
                hyd = n = i = 0;
                sum = val;
            } else switch (atm) {
                case " CA ": builder.put("C-Alpha", val);     break;
                case " C  ": builder.put("Backbone", sum / 3); break;
                case " O  ": builder.put("Main chain", sum / 4);
                    tmp = sum;
                    break;
            }
            n++;
        }
        if (n != 0) {
            double rem = (sum - tmp) / (n - 4);
            double re2 = (sum - tmp) / (n - 4 - i);
            if (!Double.isFinite(rem)) rem = re2 = 0;
            builder.put("All atomsH", sum / n);
            builder.put("All atoms", (sum - hyd) / (n - i));
            builder.put("Side chainH", rem);
            builder.put("Side chain", re2);
        }
        return builder.build();
    }

    private ObservableList<XYChart.Data<Number, Number>> getData(double[] values, int start, SummaryStatistics statistics) {
        ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
        int integer = start;
        for (Double value : values) {
            statistics.addValue(value);
            data.add(new XYChart.Data<>(integer, value));
            integer++;
        }
        return data;
    }
}