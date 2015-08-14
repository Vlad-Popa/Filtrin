package task;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.StatUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author vlad on 14/08/15.
 */
public class RewriteTask implements Runnable {

    private final BlockingQueue<String> queue;
    private final String name;

    public RewriteTask(BlockingQueue<String> queue, String name) {
        this.queue = queue;
        this.name = name.substring(0, name.lastIndexOf(".")) + "_normalized.pdb";
    }

    @Override
    public void run() {
        List<Double> doubles = Lists.newLinkedList();
        List<String> list = Lists.newLinkedList();
        while (true) {
            try {
                String line = queue.take();
                if (!line.equals("POISON")) {
                    if (line.startsWith("ATOM")) {
                        String tmp = line.substring(60, 66).trim();
                        doubles.add(Double.parseDouble(tmp));
                    }
                    list.add(line);
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        double[] array = Doubles.toArray(doubles);
        double[] values = StatUtils.normalize(array);
        int i = 0;
        File file = new File(name);
        NumberFormat format = DecimalFormat.getInstance();
        format.setMaximumFractionDigits(3);
        format.setMinimumFractionDigits(3);
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (String line : list) {
                if (line.startsWith("ATOM")) {
                    String value = format.format(values[i]);
                    if (value.length() > 5) value = value.substring(0, 5);
                    String string = line.substring(0, 60) + " " + value + line.substring(66);
                    bw.write(string);
                    bw.newLine();
                    i++;
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
