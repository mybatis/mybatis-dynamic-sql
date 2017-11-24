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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;

import org.mybatis.dynamic.sql.SortSpecification;

/**
 * This class is used for an order by phrase where there is no suitable column name
 * to use (for example a calculated column or an aggregate column). 
 * 
 * @author Jeff Butler
 */
public class SimpleSortSpecification implements SortSpecification {
    
    private String name;
    private boolean isDescending;

    private SimpleSortSpecification(String name) {
        this.name = Objects.requireNonNull(name);
    }
    
    @Override
    public SortSpecification descending() {
        SimpleSortSpecification answer = new SimpleSortSpecification(name);
        answer.isDescending = true;
        return answer;
    }

    @Override
    public String aliasOrName() {
        return name;
    }

    @Override
    public boolean isDescending() {
        return isDescending;
    }
    
    public static SimpleSortSpecification of(String name) {
        return new SimpleSortSpecification(name);
    }
}
