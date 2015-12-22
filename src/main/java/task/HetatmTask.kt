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

package task

import application.HetModel
import com.google.common.collect.HashMultimap
import com.google.common.primitives.Doubles
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXMLLoader
import javafx.scene.control.TableView
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.util.concurrent.BlockingQueue

/**
 * @author Vlad Popa on 9/5/2015.
 */
class HetatmTask(private val queue: BlockingQueue<String>) : Runnable {

    override fun run() {
        val multimap = HashMultimap.create<String, Double>()
        val view = FXMLLoader.load<TableView<HetModel>>(javaClass.getResource("/hetatm.fxml"))
        while (true) {
            val line = queue.take()
            if (!line.equals("POISON")) {
                val key = line.substring(17, 20)
                val value = line.substring(60, 66).toDouble()
                multimap.put(key, value)
            } else break
        }
        for (key in multimap.keySet()) {
            val sample = Doubles.toArray(multimap.get(key))
            val values = StatUtils.normalize(sample)
            val key2 = key + " (normalized)"
            val het1 = method1(sample, key)
            val het2 = method1(values, key2)
            view.items.addAll(het1, het2)
        }
    }

    private fun method1(array: DoubleArray, key: String) : HetModel {
        val statistics = DescriptiveStatistics(array)
        val min  = statistics.min
        val max  = statistics.max
        val mean = statistics.mean
        val std  = statistics.standardDeviation
        val het  = HetModel(SimpleStringProperty(key),
                SimpleDoubleProperty(min),
                SimpleDoubleProperty(max),
                SimpleDoubleProperty(mean),
                SimpleDoubleProperty(std))
        return het
    }
}