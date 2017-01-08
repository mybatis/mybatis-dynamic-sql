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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ListValueCondition<T> implements Condition<T> {
    private List<T> values;

    protected ListValueCondition(Stream<T> values) {
        this.values = values.collect(Collectors.toList());
    }
    
    public final Stream<T> values() {
        return values.stream().map(this::transformValue);
    }

    /**
     * This method allows subclasses to alter the value before it is placed
     * into the parameter map.  An example of this is when the case insensitive
     * conditions will change a value to upper case.
     * 
     * We do not expose the values stream because we cannot allow subclasses
     * to change the order or number of values.
     *  
     * @param value
     * @return the transformed value - in most cases the value is not changed
     */
    public T transformValue(T value) {
        return value;
    }
    
    @Override
    public void accept(ConditionVisitor<T> visitor) {
        visitor.visit(this);
    }
    
    @Override
    public int valueCount() {
        return values.size();
    }

    public abstract String render(String columnName, Stream<String> placeholders);
}
