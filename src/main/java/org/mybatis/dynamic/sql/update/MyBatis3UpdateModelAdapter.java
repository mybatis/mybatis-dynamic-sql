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
package org.mybatis.dynamic.sql.update;

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

/**
 * This adapter will render the underlying update model for MyBatis3, and then call a MyBatis mapper method.
 * 
 * @deprecated in favor of {@link UpdateDSLCompleter}. This class will be removed without direct
 *     replacement in a future version.
 * @author Jeff Butler
 *
 */
@Deprecated
public class MyBatis3UpdateModelAdapter<R> {

    private UpdateModel updateModel;
    private Function<UpdateStatementProvider, R> mapperMethod;
    
    private MyBatis3UpdateModelAdapter(UpdateModel updateModel, Function<UpdateStatementProvider, R> mapperMethod) {
        this.updateModel = Objects.requireNonNull(updateModel);
        this.mapperMethod = Objects.requireNonNull(mapperMethod);
    }
    
    public R execute() {
        return mapperMethod.apply(updateStatement());
    }
    
    private UpdateStatementProvider updateStatement() {
        return updateModel.render(RenderingStrategy.MYBATIS3);
    }
    
    public static <R> MyBatis3UpdateModelAdapter<R> of(UpdateModel updateModel,
            Function<UpdateStatementProvider, R> mapperMethod) {
        return new MyBatis3UpdateModelAdapter<>(updateModel, mapperMethod);
    }
}
