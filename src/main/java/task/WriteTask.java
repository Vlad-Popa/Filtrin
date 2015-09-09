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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Collection;

/**
 * @author Vlad Popa on 7/1/2015.
 */
public class WriteTask implements Runnable {

    private Sheet sheet;
    private Multimap<String, String> multimap;
    private String value;

    public WriteTask(Sheet sheet, String value, Multimap<String, String> multimap) {
        this.sheet = sheet;
        this.multimap = Multimaps.filterKeys(multimap, s -> s.length() == 1);
        this.value = value;
    }

    @Override
    public void run() {
        sheet.createRow(0);
        int i = 1;
        int column = 0;
        Predicate<String> filters = null;
        switch (value.substring(0, 4)) {
            case "All ": filters = s -> true;                                      break;
            case "Main": filters = s ->  s.substring(1, 3).matches("N |CA|C |O "); break;
            case "Back": filters = s ->  s.substring(1, 3).matches("N |CA|C ");    break;
            case "Side": filters = s -> !s.substring(1, 3).matches("N |CA|C |O "); break;
            case "C-Al": filters = s ->  s.substring(1, 3).equals("CA");           break;
        }

        for (String key : multimap.keySet()) {
            Collection<String> filter = Collections2.filter(multimap.get(key), filters);
            Collection<Double> values = Collections2.transform(filter, s -> {
                String string = s.substring(48, 54);
                return Double.parseDouble(string);
            });
            int j = 0;
            double[] doubles = Doubles.toArray(values);
            double[] normals = StatUtils.normalize(doubles);
            for (String line : filter) {
                String name = line.substring(0, 4).trim();
                String resName = line.substring(4, 8).trim();
                String resSeq = line.substring(11, 14).trim();
                int index = Integer.parseInt(resSeq);
                Row row = sheet.getRow(i);
                if (row == null) row = sheet.createRow(i);
                row.createCell(column + 0).setCellValue(key);
                row.createCell(column + 1).setCellValue(index);
                row.createCell(column + 2).setCellValue(resName);
                row.createCell(column + 3).setCellValue(name);
                row.createCell(column + 4).setCellValue(doubles[j]);
                row.createCell(column + 5).setCellValue(normals[j]);
                i++;
                j++;
            }
            DescriptiveStatistics setOneStats = new DescriptiveStatistics(doubles);
            DescriptiveStatistics setTwoStats = new DescriptiveStatistics(normals);
            sheet.getRow(0).createCell(column + 0).setCellValue("Chain");
            sheet.getRow(0).createCell(column + 1).setCellValue("Sequence Number");
            sheet.getRow(0).createCell(column + 2).setCellValue("Residue");
            sheet.getRow(0).createCell(column + 3).setCellValue("Atom");
            sheet.getRow(0).createCell(column + 4).setCellValue("Temperature Factor");
            sheet.getRow(0).createCell(column + 5).setCellValue("Normalized Temperature Factor");
            sheet.getRow(0).createCell(column + 6).setCellValue("Mean");
            sheet.getRow(0).createCell(column + 7).setCellValue("Standard Deviation");
            sheet.getRow(1).createCell(column + 6).setCellValue(setOneStats.getMean());
            sheet.getRow(2).createCell(column + 6).setCellValue(setTwoStats.getMean());
            sheet.getRow(1).createCell(column + 7).setCellValue(setOneStats.getStandardDeviation());
            sheet.getRow(2).createCell(column + 7).setCellValue(setTwoStats.getStandardDeviation());
            column += 8;
            i = 1;
        }
    }
}
