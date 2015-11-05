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

package application;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author Vlad Popa on 9/19/2015.
 */
public class HetModel {

    private SimpleStringProperty pdb;
    private SimpleStringProperty avg, std, min, max;

    public HetModel(String pdb,
                    String min,
                    String max,
                    String avg,
                    String std) {
        this.pdb = new SimpleStringProperty(pdb);
        this.min = new SimpleStringProperty(min);
        this.max = new SimpleStringProperty(max);
        this.avg = new SimpleStringProperty(avg);
        this.std = new SimpleStringProperty(std);
    }

    public SimpleStringProperty pdbProperty() {
        return pdb;
    }
    public SimpleStringProperty avgProperty() {
        return avg;
    }
    public SimpleStringProperty stdProperty() {
        return std;
    }
    public SimpleStringProperty minProperty() {
        return min;
    }
    public SimpleStringProperty maxProperty() {
        return max;
    }
}
