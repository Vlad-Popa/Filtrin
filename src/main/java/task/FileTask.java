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
                queue.put(line);
            }
            queue.put("POISON");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
