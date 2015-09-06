package task;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;
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

    private Multimap<String, Double> collection;
    private Multimap<String, XYChart.Series> multimap;
    private Table<String, String, SummaryStatistics> table;
    private String chainId;
    private int resSeq;

    public SeriesTask(Multimap<String, Double> collection) {
        this.collection = collection;
        this.multimap = HashMultimap.create();
        this.table = HashBasedTable.create();
    }

    @Override
    public Wrapper call() throws Exception {
        for (String key : collection.keySet()) {
            if (key.length() == 1) {
                chainId = key;
                resSeq = collection.get(key).iterator().next().intValue();
            }
        }
        for (String key : collection.keySet()) {
            if (key.length() != 1) {
                Collection<Double> values = collection.get(key);
                double[] doubles = Doubles.toArray(values);
                double[] normals = StatUtils.normalize(doubles);
                this.convertDataToSeries(doubles, key);
                this.convertDataToSeries(normals, key + "N");
            }
        }
        return new Wrapper(multimap, table);
    }

    private void convertDataToSeries(double[] values, String key) {
        SummaryStatistics stats = new SummaryStatistics();
        XYChart.Series series = new XYChart.Series();
        series.setName(chainId);
        int i = resSeq;
        for (Double value : values) {
            stats.addValue(value);
            series.getData().add(new XYChart.Data(i, value));
            i++;
        }
        multimap.put(key, series);
        table.put(key, chainId, stats);
    }
}