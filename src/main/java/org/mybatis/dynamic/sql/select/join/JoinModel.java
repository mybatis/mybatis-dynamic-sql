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
package org.mybatis.dynamic.sql.select.join;

import java.util.List;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.select.JoinSpecification;

public class JoinModel {
    private List<JoinSpecification> joinSpecifications;
    
    private JoinModel(List<JoinSpecification> joinSpecifications) {
        this.joinSpecifications = joinSpecifications;
    }

    public Stream<JoinSpecification> joinSpecifications() {
        return joinSpecifications.stream();
    }
    
    public static JoinModel of(List<JoinSpecification> joinSpecifications) {
        return new JoinModel(joinSpecifications);
    }
}
