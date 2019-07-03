/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.where;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

public class WhereDSL extends AbstractWhereDSL<WhereDSL> {

    private WhereDSL() {
        super();
    }

    private <T> WhereDSL(BindableColumn<T> column, VisitableCondition<T> condition) {
        super(column, condition);
    }

    private <T> WhereDSL(BindableColumn<T> column, VisitableCondition<T> condition, SqlCriterion<?>... subCriteria) {
        super(column, condition, subCriteria);
    }

    @Override
    protected WhereDSL getThis() {
        return this;
    }

    public static WhereDSL where() {
        return new WhereDSL();
    }

    public static <T> WhereDSL where(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new WhereDSL(column, condition);
    }

    public static <T> WhereDSL where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>... subCriteria) {
        return new WhereDSL(column, condition, subCriteria);
    }

    public WhereModel build() {
        return super.buildWhereModel();
    }
}
