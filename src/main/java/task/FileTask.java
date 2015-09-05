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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * @author Vlad Popa on 7/15/2015.
 */
public class FileTask implements Runnable {

    private final BlockingQueue<String> queue1;
    private final BlockingQueue<String> queue2;
    private final File file;

    public FileTask(BlockingQueue<String> queue1,
                    BlockingQueue<String> queue2,
                    File file) {
        this.queue1 = queue1;
        this.queue2 = queue2;
        this.file = file;
    }

    @Override
    public void run() {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            for (String line : (Iterable<String>) lines::iterator) {
                queue1.put(line);
                if (line.startsWith("HETATM")) {
                    queue2.put(line);
                }
            }
            queue1.put("POISON");
            queue2.put("POISON");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
