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

import com.google.common.collect.*;
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
        Multimap<String, Double> multimap = LinkedListMultimap.create();
        String full = "All atoms"  + condition;
        String main = "Main chain" + condition;
        String side = "Side chain" + condition;
        String back = "Backbone"   + condition;
        String atom = "C-Alpha"    + condition;
        int n = 0;
        double sum = 0;
        double tmp = 0;
        boolean flag = condition.contains("N");
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String key = entry.getKey();
            double val = entry.getValue();
            String atm = key.substring(1, 3);
            sum += val;
            if (atm.equals("N ") && n != 0) {
                double num = sum - val;
                double rem = (num - tmp) / (n - 4);
                if (!Double.isFinite(rem)) rem = 0;
                multimap.put(full, num / n);
                multimap.put(side, rem);
                sum = val;
                n = 0;
            } else switch (atm) {
                case "CA": multimap.put(atom, val);     break;
                case "C ": multimap.put(back, sum / 3); break;
                case "O ": multimap.put(main, sum / 4);
                    tmp = sum;
                    break;
            }
            n++;
        }

        if (n != 0) {
            double aDouble = (sum - tmp) / (n - 4);
            if (!Double.isFinite(aDouble)) aDouble = 0;
            multimap.put(full, sum / n);
            multimap.put(side, aDouble);
        }

        String str = Iterables.get(map.keySet(), 0);
        String idx = str.substring(11).trim();
        int resSeq = Integer.parseInt(idx);

        for (String key : multimap.keySet()) {
            ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
            SummaryStatistics statistics = new SummaryStatistics();
            double[] values = Doubles.toArray(multimap.get(key));
            if (flag) {
                values = StatUtils.normalize(values);
            }
            int i = resSeq;
            for (Double value : values) {
                if (value != 0) {
                    statistics.addValue(value);
                    data.add(new XYChart.Data<>(i, value));
                }
                i++;
            }
            bounds.put(key, statistics);
            series.put(key, new XYChart.Series<>(chainId, data));
        }
        return null;
    }
}
