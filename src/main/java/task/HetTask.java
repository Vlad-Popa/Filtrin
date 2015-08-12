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

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 8/9/2015.
 */
public class HetTask implements Callable<StatisticalSummary> {

    private final BlockingQueue<String> queue;

    public HetTask(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public StatisticalSummary call() throws Exception {
        SummaryStatistics statistics = new SummaryStatistics();
        while (true) {
            String line = queue.take();
            if (!line.equals("POISON")) {
                String tmp = line.substring(60, 66).trim();
                double val = Double.parseDouble(tmp);
                statistics.addValue(val);
            } else {
                break;
            }
        }
        return statistics;
    }
}
