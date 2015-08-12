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
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 7/31/2015.
 */
public class SeriesTask implements Callable<SummaryStatistics> {

    private String chainId;
    private String condition;
    private Map<String, Double> map;
    private Multimap<String, XYChart.Series<Number, Number>> series;
    private Multimap<String, SummaryStatistics> bounds;

    public SeriesTask(String condition, String chainId,
                      Map<String, Double> map,
                      Multimap<String, XYChart.Series<Number, Number>> series,
                      Multimap<String, SummaryStatistics> bounds) {
        this.chainId = chainId;
        this.series = series;
        this.bounds = bounds;
        this.condition = condition;
        this.map = map;
    }

    @Override
    public SummaryStatistics call() throws Exception {
        Table<String, Integer, Double> table = HashBasedTable.create();
        String full = "All atoms"  + condition;
        String main = "Main chain" + condition;
        String side = "Side chain" + condition;
        String back = "Backbone"   + condition;
        String atom = "C-Alpha"    + condition;
        int count = 0;
        int index = 0;
        double total = 0;
        double sigma = 0;
        double value;
        boolean flag = condition.contains("N");
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String key = entry.getKey();
            value = entry.getValue();
            total += value;
            switch (key.substring(1, 3)) {
                case "N ":
                    if (count != 0) {
                        if (sigma != 0) {
                            table.put(side, index, sigma / (count - 4));
                        }
                        table.put(full, index, (total - value) / count);
                        sigma = count = 0;
                        total = value;
                    }
                    String resSeq = key.substring(8);
                    index = Integer.parseInt(resSeq);
                    break;
                case "CA": table.put(atom, index, value); break;
                case "C ": table.put(back, index, total / 3.0); break;
                case "O ": table.put(main, index, total / 4.0); break;
                default:
                    sigma += value;
                    break;
            }
            count++;
        }
        
        for (String key : table.rowKeySet()) {
            ObservableList<XYChart.Data<Number, Number>> data;
            data = FXCollections.observableArrayList();
            double[] values = Doubles.toArray(table.row(key).values());
            if (flag) {
                values = StatUtils.normalize(values);
            }
            int i = 0;
            SummaryStatistics statistics = new SummaryStatistics();
            for (Integer resSeq : table.row(key).keySet()) {
                double v = values[i];
                statistics.addValue(v);
                data.add(new XYChart.Data<>(resSeq, v));
                i++;
            }
            bounds.put(key, statistics);
            series.put(key, new XYChart.Series<>(chainId, data));
        }
        return null;
    }
}
