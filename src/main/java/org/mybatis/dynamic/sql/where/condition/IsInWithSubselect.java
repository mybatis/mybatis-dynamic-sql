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
package org.mybatis.dynamic.sql.where.condition;

import org.mybatis.dynamic.sql.AbstractSubselectCondition;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;

public class IsInWithSubselect<T> extends AbstractSubselectCondition<T> {
    
    protected IsInWithSubselect(Buildable<SelectModel> selectModelBuilder) {
        super(selectModelBuilder);
    }

    public static <T> IsInWithSubselect<T> of(Buildable<SelectModel> selectModelBuilder) {
        return new IsInWithSubselect<>(selectModelBuilder);
    }

    @Override
    public String renderCondition(String columnName, String renderedSelectStatement) {
        return columnName + " in (" + renderedSelectStatement + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
