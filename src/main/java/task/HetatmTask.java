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

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.StatUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 9/5/2015.
 */
public class HetatmTask implements Callable<Map<String, double[]>> {

    private Multimap<String, String> multimap;

    public HetatmTask(Multimap<String, String> multimap) {
        Predicate<String> predicate = s -> s.length() > 1 && !s.matches("MIN|MAX");
        this.multimap = Multimaps.filterKeys(multimap, predicate);
    }

    @Override
    public Map<String, double[]> call() throws Exception {
        Map<String, double[]> map = Maps.newLinkedHashMap();
        for (String key : multimap.keySet()) {
            Collection<String> collection = multimap.get(key);
            Collection<Double> values = Collections2.transform(collection, Double::parseDouble);
            double[] doubles = Doubles.toArray(values);
            double[] normals = StatUtils.normalize(doubles);
            map.put(key, doubles);
            map.put(key + " (normalized)", normals);
        }
        return map;
    }
}
