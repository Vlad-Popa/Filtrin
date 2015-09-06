package task;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;

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
                multimap.put("All atomsH", num / n);
                multimap.put("All atoms", (num - hyd) / (n - i));
                multimap.put("Side chainH", rem);
                multimap.put("Side chain",  re2);
                hyd = n = i = 0;
                sum = val;
            } else switch (atm) {
                case " CA ": multimap.put("C-Alpha",    val);     break;
                case " C  ": multimap.put("Backbone",   sum / 3); break;
                case " O  ": multimap.put("Main chain", sum / 4);
                    tmp = sum;
                    break;
            }
            n++;
        }
        if (n != 0) {
            double rem = (sum - tmp) / (n - 4);
            double re2 = (sum - tmp) / (n - 4 - i);
            if (!Double.isFinite(rem)) rem = re2 = 0;
            multimap.put("All atomsH", sum / n);
            multimap.put("All atoms", (sum - hyd) / (n - i));
            multimap.put("Side chainH", rem);
            multimap.put("Side chain", re2);
        }

        String str = Iterables.get(collection, 0);
        String idx = str.substring(11, 15).trim();
        double seq = Double.parseDouble(idx);
        multimap.put(chainId, seq);
        return multimap.build();
    }
}
