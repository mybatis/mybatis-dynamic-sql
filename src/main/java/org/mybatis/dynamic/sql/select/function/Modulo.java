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

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class Modulo<T extends Number> extends AbstractFunction<T, Modulo<T>> {
    
	private Integer modulo;
	
    private Modulo(BindableColumn<T> firstColumn, Integer modulo) {
        super(firstColumn);
        this.modulo = modulo;
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return column.renderWithTableAlias(tableAliasCalculator)
                + " % " //$NON-NLS-1$
                + modulo;
    }

    public static <T extends Number> Modulo<T> of(BindableColumn<T> firstColumn, Integer modulo) {
        return new Modulo<>(firstColumn, modulo);
    }

	@Override
	protected Modulo<T> copy() {
		return new Modulo<>(column, modulo);
	}
}
