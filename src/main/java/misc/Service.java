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

package misc;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Vlad Popa on 8/6/2015.
 */
public class Service {

    public static final ListeningExecutorService INSTANCE =
            MoreExecutors.listeningDecorator(
                    MoreExecutors.getExitingExecutorService(
                            (ThreadPoolExecutor) Executors.newFixedThreadPool(
                                    Runtime.getRuntime().availableProcessors())));

    private Service() {
        throw new AssertionError("Instantiating singleton class.");
    }
}
