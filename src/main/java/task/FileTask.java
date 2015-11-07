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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * @author vlad on 14/08/15.
 */
public class FileTask implements Runnable {

    private final BlockingQueue<String> queue;
    private final Path path;

    public FileTask(BlockingQueue<String> queue, Path path) {
        this.queue = queue;
        this.path = path;
    }

    @Override
    public void run() {
        try (Stream<String> lines = Files.lines(path)) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                    queue.put(line);
                }
            }
            queue.put("POISON");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
