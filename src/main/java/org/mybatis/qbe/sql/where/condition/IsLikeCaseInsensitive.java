/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.where.condition;

public class IsLikeCaseInsensitive extends IsLike {
    private IsLikeCaseInsensitive(String value) {
        super(value);
    }
    
    public static IsLikeCaseInsensitive of(String value) {
        return new IsLikeCaseInsensitive(value);
    }
    
    @Override
    public String composeLeftSide(String fieldName) {
        return String.format("upper(%s)", fieldName);
    }
    
    @Override
    public String value() {
        return super.value().toUpperCase();
    }
}