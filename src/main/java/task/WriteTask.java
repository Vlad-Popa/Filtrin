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
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Vlad Popa on 7/1/2015.
 */
public class WriteTask implements Callable<Table<String, String, CellReference>> {

    private String value;
    private Sheet sheet;
    private Table<String, String, Double> table;

    public WriteTask(Table<String, String, Double> table, Sheet sheet, String value) {
        this.table = table;
        this.sheet = sheet;
        this.value = value;
    }

    @Override
    public Table<String, String, CellReference> call() throws Exception {
        sheet.createRow(0);
        int i = 1;
        int column = 0;
        Predicate<String> main = s -> s.substring(1, 3).matches("N |CA|C |O ");
        Predicate<String> side = s -> !s.substring(1, 3).matches("N |CA|C |O ");
        Predicate<String> back = s -> s.substring(1, 3).matches("N |CA|C ");
        Predicate<String> atom = s -> s.substring(1, 3).equals("CA");
        Predicate<String> full = s -> true;
        for (String string : table.rowKeySet()) {
            DescriptiveStatistics setOneStats = new DescriptiveStatistics();
            DescriptiveStatistics setTwoStats = new DescriptiveStatistics();
            Map<String, Double> map = table.row(string);
            Map<String, Double> filter = null;
            switch (value) {
                case "All atoms":   filter = Maps.filterKeys(map, full); break;
                case "Main chain":  filter = Maps.filterKeys(map, main); break;
                case "Backbone":    filter = Maps.filterKeys(map, back); break;
                case "Side chain":  filter = Maps.filterKeys(map, side); break;
                case "C-Alpha":     filter = Maps.filterKeys(map, atom); break;
            }
            assert filter != null;
            double[] array = Doubles.toArray(filter.values());
            double[] values = StatUtils.normalize(array);
            int j = 0;
            for (String key : filter.keySet()) {
                String name = key.substring(0, 4).trim();
                String resName = key.substring(5, 8);
                String resSeq = key.substring(8);
                int index = Integer.parseInt(resSeq);
                Row row = sheet.getRow(i);
                if (row == null) row = sheet.createRow(i);
                row.createCell(column + 0).setCellValue(string);
                row.createCell(column + 1).setCellValue(index);
                row.createCell(column + 2).setCellValue(resName);
                row.createCell(column + 3).setCellValue(name);
                row.createCell(column + 4).setCellValue(array[j]);
                row.createCell(column + 5).setCellValue(values[j]);
                setOneStats.addValue(array[j]);
                setTwoStats.addValue(values[j]);
                i++;
                j++;
            }
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
        return TreeBasedTable.create();
    }
}
