package task;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 9/6/2015.
 */
public class SortTask implements Callable<Multimap<String, Double>> {

    private Collection<String> collection;
    private String chainId;

    public SortTask(Collection<String> collection, String chainId) {
        this.collection = collection;
        this.chainId = chainId;
    }

    @Override
    public Multimap<String, Double> call() throws Exception {
        ImmutableMultimap.Builder<String, Double> multimap = ImmutableMultimap.builder();
        double sum = 0;
        double tmp = 0;
        double h = 0;
        int n = 0;
        int i = 0;
        for (String line : collection) {
            String atm = line.substring(0, 4);
            String fac = line.substring(48, 54);
            double val = Double.parseDouble(fac);
            sum += val;
            switch (atm) {
                case " N  ":
                    if (n != 0) {
                        double num = sum - val;
                        double rem = tmp != 0 ? tmp / (n - 4) : 0;
                        double re2 = tmp != 0 ? (tmp - h) / (n - i - 4) : 0;
                        if (Double.isNaN(re2)) re2 = h / i;
                        multimap.put("All atomsH",  num / n);
                        multimap.put("All atoms",  (num - h) / (n - i));
                        multimap.put("Side chainH", rem);
                        multimap.put("Side chain",  re2);
                        n = i = 0;
                        tmp = h = 0;
                        sum = val;
                    }
                    break;
                case " CA ": multimap.put("C-Alpha",    val);     break;
                case " C  ": multimap.put("Backbone",   sum / 3); break;
                case " O  ": multimap.put("Main chain", sum / 4); break;
                default:
                    if (atm.contains("H")) {
                        h += val;
                        i++;
                    }
                    tmp += val;
                    break;
            }
            n++;
        }
        if (n != 0) {
            double rem = tmp != 0 ? tmp / (n - 4) : 0;
            double re2 = tmp != 0 ? (tmp - h) / (n - i - 4) : 0;
            if (Double.isNaN(re2)) re2 = h / i;
            multimap.put("All atomsH", sum / n);
            multimap.put("All atoms", (sum - h) / (n - i));
            multimap.put("Side chainH", rem);
            multimap.put("Side chain",  re2);
        }

        String str = Iterables.get(collection, 0);
        String idx = str.substring(11, 15).trim();
        double seq = Double.parseDouble(idx);
        multimap.put(chainId, seq);
        return multimap.build();
    }
}
