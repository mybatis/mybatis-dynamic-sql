/**
 *    Copyright 2016-2020 the original author or authors.
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
package examples.animal.data;

import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.select.function.AbstractMultipleColumnArithmeticFunction;

public class DeprecatedAdd<T extends Number> extends AbstractMultipleColumnArithmeticFunction<T, DeprecatedAdd<T>> {

    private DeprecatedAdd(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            List<BasicColumn> subsequentColumns) {
        super(firstColumn, secondColumn, subsequentColumns);
    }

    @Override
    protected DeprecatedAdd<T> copy() {
        return new DeprecatedAdd<>(column, secondColumn, subsequentColumns);
    }

    @Override
    protected String operator() {
        return "+"; //$NON-NLS-1$
    }

    public static <T extends Number> DeprecatedAdd<T> of(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return new DeprecatedAdd<>(firstColumn, secondColumn, Arrays.asList(subsequentColumns));
    }
}
