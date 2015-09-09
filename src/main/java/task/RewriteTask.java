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
import org.apache.commons.math3.stat.StatUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
        List<String> list = Lists.newLinkedList();
        ImmutableTable.Builder<String, String, Double> builder = ImmutableTable.builder();
        while (true) {
            try {
                String line = queue.take();
                if (!line.equals("POISON")) {
                    if (line.startsWith("ATOM")) {
                        String key = line.substring(21, 22);
                        String tmp = line.substring(60, 66);
                        double val = Double.parseDouble(tmp);
                        builder.put(key, line, val);
                    }
                    list.add(line);
                } else break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Table<String, String, Double> table = builder.build();
        try (FileWriter fw = new FileWriter(file); BufferedWriter bw = new BufferedWriter(fw)) {
            Iterator<String> iterator = table.columnKeySet().iterator();
            String current = iterator.next();
            String chainId = current.substring(21, 22);
            Collection<Double> collection = table.row(chainId).values();
            double[] doubles = Doubles.toArray(collection);
            double[] normals = StatUtils.normalize(doubles);
            int i = 0;
            for (String line : list) {
                if (line.equals(current)) {
                    String key = current.substring(21, 22);
                    if (!key.equals(chainId)) {
                        collection = table.row(key).values();
                        doubles = Doubles.toArray(collection);
                        normals = StatUtils.normalize(doubles);
                        chainId = key;
                        i = 0;
                    }
                    String factor = String.valueOf(normals[i]).substring(0, 5);
                    String str = line.substring(0, 60) + ' ' + factor + line.substring(66);
                    bw.write(str);
                    bw.newLine();
                    i++;
                    if (iterator.hasNext()) {
                        current = iterator.next();
                    }
                } else {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
