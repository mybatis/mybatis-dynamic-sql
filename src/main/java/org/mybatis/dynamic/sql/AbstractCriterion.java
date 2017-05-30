/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractCriterion<T, S extends AbstractColumn<T>, R extends AbstractCriterion<?, ?, ?>> {
    protected S column;
    protected Condition<T> condition;
    protected Optional<String> connector;
    protected Optional<List<R>> subCriteria;
    
    public Optional<String> connector() {
        return connector;
    }
    
    public S column() {
        return column;
    }
    
    public Condition<T> condition() {
        return condition;
    }
    
    public Optional<Stream<R>> subCriteria() {
        return subCriteria.flatMap(sc -> Optional.of(sc.stream()));
    }
}
