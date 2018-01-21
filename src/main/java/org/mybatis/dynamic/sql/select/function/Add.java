/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.select.function;

import java.util.List;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;

public class Add<T extends Number> extends AbstractMultipleColumnArithmeticFunction<T, Add<T>> {
    
    private Add(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            List<BasicColumn> subsequentColumns) {
        super(firstColumn, secondColumn, subsequentColumns);
    }

    @Override
    protected Add<T> copy() {
        return new Add<>(column, secondColumn, subsequentColumns);
    }

    @Override
    protected String operator() {
        return "+"; //$NON-NLS-1$
    }

    public static <T extends Number> Add<T> of(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            List<BasicColumn> subsequentColumns) {
        return new Add<>(firstColumn, secondColumn, subsequentColumns);
    }
}
