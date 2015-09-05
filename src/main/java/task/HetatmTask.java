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

    public HetatmTask(Multimap<String, String> multimap) {
        this.multimap = multimap;
    }

    @Override
    public Table<String, String, Double> call() throws Exception {
        Predicate<String> predicate = s -> s.length() > 1 && !s.matches("MIN|MAX");
        Table<String, String, Double> table = HashBasedTable.create();
        Multimap<String, String> filtered = Multimaps.filterKeys(multimap, predicate);
        for (String key : filtered.keySet()) {
            Collection<String> collection = filtered.get(key);
            Collection<Double> values = Collections2.transform(collection, Double::parseDouble);
            double[] doubles = Doubles.toArray(values);
            double[] normals = StatUtils.normalize(doubles);
            DescriptiveStatistics stats1 = new DescriptiveStatistics(doubles);
            DescriptiveStatistics stats2 = new DescriptiveStatistics(normals);
            table.put(key, "n", (double) stats1.getN());
            table.put(key, "min", stats1.getMin());
            table.put(key, "max", stats1.getMax());
            table.put(key, "avg", stats1.getMean());
            table.put(key, "std", stats1.getStandardDeviation());
            table.put(key + "N", "n", (double) stats2.getN());
            table.put(key + "N", "min", stats2.getMin());
            table.put(key + "N", "max", stats2.getMax());
            table.put(key + "N", "avg", stats2.getMean());
            table.put(key + "N", "std", stats2.getStandardDeviation());
        }
        return table;
    }
}
