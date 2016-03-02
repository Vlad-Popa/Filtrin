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

import java.io.File
import java.util.concurrent.BlockingQueue

/**
 * @author vlad on 14/08/15.
 */
class FileTask(private val queue: BlockingQueue<String>, private val file: File) : Runnable {

    override fun run() {
        file.forEachLine {
            if (it.startsWith("ATOM")) {
                queue.put(it)
            }
        }
        queue.put("POISON")
    }
}