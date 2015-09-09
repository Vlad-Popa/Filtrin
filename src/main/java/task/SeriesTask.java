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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import application.Wrapper;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 9/2/2015.
 */
public class SeriesTask implements Callable<Wrapper> {

    private Multimap<String, Double> collection;
    private Multimap<String, XYChart.Series<Number, Number>> multimap;
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
            if (value != 0) {
                stats.addValue(value);
                series.getData().add(new XYChart.Data(i, value));
            }
            i++;
        }
        multimap.put(key, series);
        table.put(key, chainId, stats);
    }
}