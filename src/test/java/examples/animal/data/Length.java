/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.animal.data;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.function.AbstractTypeConvertingFunction;

public class Length extends AbstractTypeConvertingFunction<Object, Integer, Length> {
    private Length(BindableColumn<Object> column) {
        super(column);
    }

    @Override
    public Optional<JDBCType> jdbcType() {
        return Optional.of(JDBCType.INTEGER);
    }

    @Override
    public Optional<String> typeHandler() {
        return Optional.empty();
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "length(" //$NON-NLS-1$
                + column.renderWithTableAlias(tableAliasCalculator)
                + ")"; //$NON-NLS-1$
    }

    @Override
    protected Length copy() {
        return new Length(column);
    }

    public static Length length(BindableColumn<?> column) {
        @SuppressWarnings("unchecked")
        BindableColumn<Object> c = (BindableColumn<Object>) column;
        return new Length(c);
    }
}
