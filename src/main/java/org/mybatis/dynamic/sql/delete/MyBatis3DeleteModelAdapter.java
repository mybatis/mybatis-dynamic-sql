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
package org.mybatis.dynamic.sql.delete;

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

/**
 * This adapter will render the underlying delete model for MyBatis3, and then call a MyBatis mapper method.
 *
 * @deprecated in favor of {@link DeleteDSLCompleter}. This class will be removed without replacement in a
 *     future version
 * 
 * @author Jeff Butler
 *
 */
@Deprecated
public class MyBatis3DeleteModelAdapter<R> {

    private DeleteModel deleteModel;
    private Function<DeleteStatementProvider, R> mapperMethod;
    
    private MyBatis3DeleteModelAdapter(DeleteModel deleteModel, Function<DeleteStatementProvider, R> mapperMethod) {
        this.deleteModel = Objects.requireNonNull(deleteModel);
        this.mapperMethod = Objects.requireNonNull(mapperMethod);
    }
    
    public R execute() {
        return mapperMethod.apply(deleteStatement());
    }
    
    private DeleteStatementProvider deleteStatement() {
        return deleteModel.render(RenderingStrategy.MYBATIS3);
    }
    
    public static <R> MyBatis3DeleteModelAdapter<R> of(DeleteModel deleteModel,
            Function<DeleteStatementProvider, R> mapperMethod) {
        return new MyBatis3DeleteModelAdapter<>(deleteModel, mapperMethod);
    }
}
