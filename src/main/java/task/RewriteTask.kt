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

import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.BlockingQueue

/**
 * @author vlad on 14/08/15.
 */
class RewriteTask(private val queue: BlockingQueue<String>, private val path: Path) : Runnable {

    override fun run() {
        val statistics = DescriptiveStatistics()
        val linkedList = linkedListOf<String>()
        var chainId = ' '
        while (true) {
            val line = queue.take()
            if (!line.equals("POISON")) {
                val currentId = line[21]
                if (chainId == ' ') {
                    chainId = currentId
                } else if (chainId != currentId) {
                    val builder = StringBuilder()
                    val values = StatUtils.normalize(statistics.values)
                    var i = 0
                    for (item in linkedList) {
                        builder.append(line.substring(0, 60))
                        builder.append(" ").append(values[i])
                        builder.append(line.substring(66))
                        builder.append("\n")
                        i++;
                    }
                    val bytes = builder.toString().toByteArray()
                    Files.write(path, bytes, StandardOpenOption.APPEND)
                    statistics.clear()
                    chainId = currentId
                }
                val tempFactor = line.substring(60, 66).toDouble()
                statistics.addValue(tempFactor)
                linkedList.add(line)
            } else break
        }
    }
}