/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.select.HavingModel;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public abstract class AbstractBooleanOperationsFinisher<T extends AbstractBooleanOperationsFinisher<T>>
        implements BooleanOperations<T> {
    protected @Nullable SqlCriterion initialCriterion;
    protected final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

    protected void setInitialCriterion(@Nullable SqlCriterion initialCriterion) {
        this.initialCriterion = initialCriterion;
    }

    public void setInitialCriterion(@Nullable SqlCriterion initialCriterion, StatementType statementType) {
        Validator.assertTrue(this.initialCriterion == null, statementType.messageNumber());
        setInitialCriterion(initialCriterion);
    }

    public void initialize(AbstractBooleanOperationsFinisher<?> other, StatementType statementType) {
        setInitialCriterion(other.initialCriterion, statementType);
        subCriteria.addAll(other.subCriteria);
    }

    @Override
    public T addSubCriterion(AndOrCriteriaGroup subCriterion) {
        subCriteria.add(subCriterion);
        return getThis();
    }

    protected HavingModel toHavingModel() {
        return new HavingModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }

    protected EmbeddedWhereModel toWhereModel() {
        return new EmbeddedWhereModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }

    protected abstract T getThis();

    public enum StatementType {
        WHERE("ERROR.32"), //$NON-NLS-1$
        HAVING("ERROR.31"); //$NON-NLS-1$

        private final String messageNumber;

        public String messageNumber() {
            return messageNumber;
        }

        StatementType(String messageNumber) {
            this.messageNumber = messageNumber;
        }
    }
}
