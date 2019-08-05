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

import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.MyBatis3DeleteModelAdapter;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

/**
 * This adapter will render the underlying delete model for MyBatis3, and then call a MyBatis mapper method.
 * This version of the adapter is preferred over {@link MyBatis3DeleteModelAdapter} because it is not generic
 * and does not force a box/unbox for mappers that always return a primitive int.
 * 
 * @see MyBatis3Utils
 * 
 * @author Jeff Butler
 *
 */
public class MyBatis3DeleteModelToIntAdapter {

    private DeleteModel deleteModel;
    private ToIntFunction<DeleteStatementProvider> mapperMethod;
    
    private MyBatis3DeleteModelToIntAdapter(DeleteModel deleteModel,
            ToIntFunction<DeleteStatementProvider> mapperMethod) {
        this.deleteModel = Objects.requireNonNull(deleteModel);
        this.mapperMethod = Objects.requireNonNull(mapperMethod);
    }
    
    public int execute() {
        return mapperMethod.applyAsInt(deleteStatement());
    }
    
    private DeleteStatementProvider deleteStatement() {
        return deleteModel.render(RenderingStrategy.MYBATIS3);
    }
    
    public static MyBatis3DeleteModelToIntAdapter of(DeleteModel deleteModel,
            ToIntFunction<DeleteStatementProvider> mapperMethod) {
        return new MyBatis3DeleteModelToIntAdapter(deleteModel, mapperMethod);
    }
}
