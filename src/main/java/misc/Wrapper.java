package misc;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * @author Vlad Popa on 9/5/2015.
 */
public class Wrapper {

    private Multimap<String, XYChart.Series<Number, Number>> multimap;
    private Table<String, String, SummaryStatistics> table;

    public Wrapper(Multimap<String, XYChart.Series<Number, Number>> multimap,
                   Table<String, String, SummaryStatistics> table) {
        this.multimap = multimap;
        this.table = table;
    }

    public Multimap<String, XYChart.Series<Number, Number>> getMultimap() {
        return multimap;
    }
    public Table<String, String, SummaryStatistics> getTable() {
        return table;
    }
}
