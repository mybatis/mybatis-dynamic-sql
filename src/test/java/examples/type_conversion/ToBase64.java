/*
 *    Copyright 2016-2024 the original author or authors.
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

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.function.AbstractTypeConvertingFunction;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class ToBase64 extends AbstractTypeConvertingFunction<byte[], String, ToBase64> {

    protected ToBase64(BasicColumn column) {
        super(column);
    }

    @Override
    public Optional<JDBCType> jdbcType() {
        return Optional.of(JDBCType.VARCHAR);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return column.render(renderingContext)
                .mapFragment(f -> "TO_BASE64(" + f + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected ToBase64 copy() {
        return new ToBase64(column);
    }

    public static ToBase64 toBase64(BindableColumn<byte[]> column) {
        return new ToBase64(column);
    }
}
