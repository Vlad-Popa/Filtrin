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
import com.google.common.collect.Multimap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 7/17/2015.
 */
public class DataTask implements Callable<Multimap<String, String>> {

    private final BlockingQueue<String> queue;

    public DataTask(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public Multimap<String, String> call() throws Exception {
        ImmutableMultimap.Builder<String, String> multimap = ImmutableMultimap.builder();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        boolean nID = true;
        while (true) {
            String line = queue.take();
            if (!line.equals("POISON")) {
                char alt = line.charAt(16);
                if (line.startsWith("TER")) {
                    String str = line.substring(23, 26).trim();
                    int resSeq = Integer.parseInt(str);
                    max = Math.max(max, resSeq);
                    nID = true;
                } else if (alt == (' ') || alt == 'A') {
                    if (line.startsWith("ATOM")) {
                        String key = line.substring(21, 22);
                        String val = line.substring(12, 66);
                        multimap.put(key, val);
                        if (nID) {
                            String str = line.substring(23, 26).trim();
                            int resSeq = Integer.parseInt(str);
                            min = Math.min(min, resSeq);
                            nID = false;
                        }
                    } else if (line.startsWith("HETATM")) {
                        String key = line.substring(17, 20);
                        String val = line.substring(60, 66);
                        multimap.put(key, val);
                    }
                }
            } else break;
        }
        multimap.put("MIN", String.valueOf(min));
        multimap.put("MAX", String.valueOf(max));
        return multimap.build();
    }
}