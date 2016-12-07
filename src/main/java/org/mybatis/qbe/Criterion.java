/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Criterion<T, S extends Field<T>, R extends Criterion<?, ?, ?>> {
    protected S field;
    protected Condition<T> condition;
    protected String connector;
    protected List<R> subCriteria;
    
    protected Criterion(Stream<R> subCriteria) {
        this.subCriteria = subCriteria.collect(Collectors.toList());
    }
    
    public Optional<String> connector() {
        return Optional.ofNullable(connector);
    }
    
    public S field() {
        return field;
    }
    
    public Condition<T> condition() {
        return condition;
    }
    
    public boolean hasSubCriteria() {
        return !subCriteria.isEmpty();
    }
 
    public Stream<R> subCriteria() {
        return subCriteria.stream();
    }
}
