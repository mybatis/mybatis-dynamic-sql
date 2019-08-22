/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.select;

import java.util.function.Function;

import org.mybatis.dynamic.sql.util.Buildable;

@FunctionalInterface
public interface CountDSLCompleter extends
        Function<CountDSL<SelectModel>, Buildable<SelectModel>> {
    
    /**
     * Returns a completer that can be used to count every row in a table.
     * 
     * @return the completer that will count every row in a table
     */
    static CountDSLCompleter allRows() {
        return c -> c;
    }
}
