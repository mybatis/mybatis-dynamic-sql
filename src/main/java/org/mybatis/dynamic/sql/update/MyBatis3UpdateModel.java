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
package org.mybatis.dynamic.sql.update;

import java.util.function.Function;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateStatement;

/**
 * This UpdateModel will render the underlying update model for MyBatis3, and then call a MyBatis mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class MyBatis3UpdateModel {

    private UpdateModel updateModel;
    private Function<UpdateStatement, Integer> mapperMethod;
    
    private MyBatis3UpdateModel(UpdateModel updateModel, Function<UpdateStatement, Integer> mapperMethod) {
        this.updateModel = updateModel;
        this.mapperMethod = mapperMethod;
    }
    
    public int execute() {
        return mapperMethod.apply(updateModel.render(RenderingStrategy.MYBATIS3));
    }
    
    public static MyBatis3UpdateModel of(UpdateModel updateModel, Function<UpdateStatement, Integer> mapperMethod) {
        return new MyBatis3UpdateModel(updateModel, mapperMethod);
    }
}
