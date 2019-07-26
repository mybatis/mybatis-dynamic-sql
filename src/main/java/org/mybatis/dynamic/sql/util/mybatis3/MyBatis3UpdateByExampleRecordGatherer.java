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
package org.mybatis.dynamic.sql.util.mybatis3;

import java.util.Objects;
import java.util.function.ToIntFunction;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

public class MyBatis3UpdateByExampleRecordGatherer<T> {
    private SqlTable table;
    private MyBatis3UpdateByExampleHelper helper;
    private ToIntFunction<UpdateStatementProvider> mapper;
    private MyBatis3UpdateByExampleValueSetter<T> valueSetter;
    
    private MyBatis3UpdateByExampleRecordGatherer(MyBatis3UpdateByExampleRecordGatherer.Builder<T> builder) {
        helper = Objects.requireNonNull(builder.helper);
        mapper = Objects.requireNonNull(builder.mapper);
        valueSetter = Objects.requireNonNull(builder.valueSetter);
        table = Objects.requireNonNull(builder.table);
    }
    
    public int usingRecord(T record) {
        return valueSetter.andThen(helper)
                .apply(record, UpdateDSL.updateWithMapper(p -> mapper.applyAsInt(p), table))
                .build()
                .execute();
    }
    
    public static class Builder<T> {
        private SqlTable table;
        private MyBatis3UpdateByExampleHelper helper;
        private ToIntFunction<UpdateStatementProvider> mapper;
        private MyBatis3UpdateByExampleValueSetter<T> valueSetter;
        
        public MyBatis3UpdateByExampleRecordGatherer.Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public MyBatis3UpdateByExampleRecordGatherer.Builder<T> withHelper(MyBatis3UpdateByExampleHelper helper) {
            this.helper = helper;
            return this;
        }

        public MyBatis3UpdateByExampleRecordGatherer.Builder<T> withMapper(
                ToIntFunction<UpdateStatementProvider> mapper) {
            this.mapper = mapper;
            return this;
        }
        
        public MyBatis3UpdateByExampleRecordGatherer.Builder<T> withValueSetter(
                MyBatis3UpdateByExampleValueSetter<T> valueSetter) {
            this.valueSetter = valueSetter;
            return this;
        }
        
        public MyBatis3UpdateByExampleRecordGatherer<T> build() {
            return new MyBatis3UpdateByExampleRecordGatherer<>(this);
        }
    }
}
