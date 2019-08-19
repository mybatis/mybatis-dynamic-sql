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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

/**
 * This adapter will render the underlying select model for MyBatis3, and then call a MyBatis mapper method.
 *
 * @deprecated in favor is {@link SelectDSLCompleter}. This class will be removed without direct replacement
 *     in a future version
 *     
 * @author Jeff Butler
 *
 */
@Deprecated
public class MyBatis3SelectModelAdapter<R> {

    private SelectModel selectModel;
    private Function<SelectStatementProvider, R> mapperMethod;
    
    private MyBatis3SelectModelAdapter(SelectModel selectModel, Function<SelectStatementProvider, R> mapperMethod) {
        this.selectModel = Objects.requireNonNull(selectModel);
        this.mapperMethod = Objects.requireNonNull(mapperMethod);
    }
    
    public R execute() {
        return mapperMethod.apply(selectStatement());
    }
    
    private SelectStatementProvider selectStatement() {
        return selectModel.render(RenderingStrategy.MYBATIS3);
    }
    
    public static <R> MyBatis3SelectModelAdapter<R> of(SelectModel selectModel,
            Function<SelectStatementProvider, R> mapperMethod) {
        return new MyBatis3SelectModelAdapter<>(selectModel, mapperMethod);
    }
}
