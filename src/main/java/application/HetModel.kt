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

package application

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty

/**
 * @author Vlad Popa on 9/19/2015.
 */
data class HetModel(private val pdb: SimpleStringProperty,
                    private val avg: SimpleDoubleProperty,
                    private val std: SimpleDoubleProperty,
                    private val min: SimpleDoubleProperty,
                    private val max: SimpleDoubleProperty)
