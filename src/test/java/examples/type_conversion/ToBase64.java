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
package examples.type_conversion;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.function.AbstractTypeConvertingFunction;

public class ToBase64 extends AbstractTypeConvertingFunction<byte[], String, ToBase64> {

    protected ToBase64(BindableColumn<byte[]> column) {
        super(column);
    }

    @Override
    public Optional<JDBCType> jdbcType() {
        return Optional.of(JDBCType.VARCHAR);
    }

    @Override
    public Optional<String> typeHandler() {
        return Optional.empty();
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "TO_BASE64(" //$NON-NLS-1$
                + column.renderWithTableAlias(tableAliasCalculator)
                + ")"; //$NON-NLS-1$
    }

    @Override
    protected ToBase64 copy() {
        return new ToBase64(column);
    }

    public static ToBase64 toBase64(BindableColumn<byte[]> column) {
        return new ToBase64(column);
    }
}
