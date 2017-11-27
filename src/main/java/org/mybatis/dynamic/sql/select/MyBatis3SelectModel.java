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
package org.mybatis.dynamic.sql.select;

import java.util.function.Function;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatement;

/**
 * This SelectModel will render the underlying select model for MyBatis3, and then call a MyBatis mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class MyBatis3SelectModel<R> {

    private SelectModel selectModel;
    private Function<SelectStatement, R> mapperMethod;
    
    private MyBatis3SelectModel(SelectModel selectModel, Function<SelectStatement, R> mapperMethod) {
        this.selectModel = selectModel;
        this.mapperMethod = mapperMethod;
    }
    
    public R execute() {
        return mapperMethod.apply(selectModel.render(RenderingStrategy.MYBATIS3));
    }
    
    public static <R> MyBatis3SelectModel<R> of(SelectModel selectModel, Function<SelectStatement, R> mapperMethod) {
        return new MyBatis3SelectModel<>(selectModel, mapperMethod);
    }
}
