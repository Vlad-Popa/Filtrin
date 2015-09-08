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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.StatUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

/**
 * @author vlad on 14/08/15.
 */
public class RewriteTask implements Runnable {

    private final BlockingQueue<String> queue;
    private final File file;

    public RewriteTask(BlockingQueue<String> queue, Path path) {
        String toString = path.toString();
        int lastIndexOf = toString.lastIndexOf(".");
        String fileName = toString.substring(0, lastIndexOf) + "_normalized.pdb";
        this.file = new File(fileName);
        this.queue = queue;
    }

    @Override
    public void run() {
        Multimap<String, String> chains = LinkedListMultimap.create();
        Multimap<String, Double> values = LinkedListMultimap.create();
        while (true) {
            try {
                String line = queue.take();
                if (!line.equals("POISON")) {
                    String key = line.substring(21, 22);
                    if (line.startsWith("ATOM")) {
                        String tmp = line.substring(60, 66);
                        double val = Double.parseDouble(tmp);
                        values.put(key, val);
                    }
                    chains.put(key, line);
                } else break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter fw = new FileWriter(file); BufferedWriter bw = new BufferedWriter(fw)) {
            for (String key : chains.keySet()) {
                Collection<Double> collection = values.get(key);
                double[] doubles = Doubles.toArray(collection);
                double[] normals = StatUtils.normalize(doubles);
                int i = 0;
                for (String line : chains.get(key)) {
                    if (line.startsWith("ATOM")) {
                        String factor = String.valueOf(normals[i]).substring(0, 5);
                        String string = line.substring(0, 60) + ' ' + factor + line.substring(66);
                        bw.write(string);
                        bw.newLine();
                        i++;
                    } else {
                        bw.write(line);
                        bw.newLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
