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
            String atm = line.substring(0, 4).trim();
            String fac = line.substring(48, 54);
            double val = Double.parseDouble(fac);
            sum += val;
            switch (atm) {
                case "N":
                    if (n != 0) {
                        double num = sum - val;
                        multimap.put("All atomsH",  num / n);
                        multimap.put("All atoms",  (num - h) / (n - i));
                        if (tmp != 0) {
                            multimap.put("Side chainH", tmp / (n - 4));
                            multimap.put("Side chain", (tmp - h) / (n - i - 4));
                        } else {
                            if (h != 0) {
                                multimap.put("Side chainH", (h / i));
                            } else {
                                multimap.put("Side chainH", 0.0);
                            }
                            multimap.put("Side chain", 0.0);
                        }
                        n = 0;
                        i = 0;
                        h = 0;
                        tmp = 0;
                        sum = val;
                    }
                    break;
                case "CA": multimap.put("C-Alpha",   val);     break;
                case "C": multimap.put("Backbone",   sum / 3); break;
                case "O": multimap.put("Main chain", sum / 4); break;
                default:
                    if (atm.startsWith("H")) {
                        h += val;
                        i++;
                    }
                    tmp += val;
                    break;
            }
            n++;
        }
        if (n != 0) {
            multimap.put("All atomsH", sum / n);
            multimap.put("All atoms", (sum - h) / (n - i));
            if (tmp != 0) {
                multimap.put("Side chainH", tmp / (n - 4));
                multimap.put("Side chain", (tmp - h) / (n - i - 4));
            } else {
                if (h != 0) {
                    multimap.put("Side chainH", (h / i));
                } else {
                    multimap.put("Side chainH", 0.0);
                }
                multimap.put("Side chain", 0.0);
            }
        }
        String str = Iterables.get(collection, 0);
        String idx = str.substring(11, 15).trim();
        double seq = Double.parseDouble(idx);
        multimap.put(chainId, seq);
        return multimap.build();
    }
}
