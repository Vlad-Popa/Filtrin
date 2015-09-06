package task;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 9/5/2015.
 */
public class HetatmTask implements Callable<Table<String, String, Double>> {

    private Multimap<String, String> multimap;
    private Table<String, String, Double> table;

    public HetatmTask(Multimap<String, String> multimap) {
        Predicate<String> predicate = s -> s.length() > 1 && !s.matches("MIN|MAX");
        this.multimap = Multimaps.filterKeys(multimap, predicate);
        this.table = HashBasedTable.create();
    }

    @Override
    public Table<String, String, Double> call() throws Exception {
        for (String key : multimap.keySet()) {
            Collection<String> collection = multimap.get(key);
            Collection<Double> values = Collections2.transform(collection, Double::parseDouble);
            double[] doubles = Doubles.toArray(values);
            double[] normals = StatUtils.normalize(doubles);
            this.populateTable(doubles, key);
            this.populateTable(normals, key + "N");
        }
        return table;
    }

    private void populateTable(double[] values, String key) {
        DescriptiveStatistics stats1 = new DescriptiveStatistics(values);
        table.put(key, "n", (double) stats1.getN());
        table.put(key, "min", stats1.getMin());
        table.put(key, "max", stats1.getMax());
        table.put(key, "avg", stats1.getMean());
        table.put(key, "std", stats1.getStandardDeviation());
    }
}
