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
package org.mybatis.qbe.sql.where.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderedCriterion {

    private String whereClauseFragment;
    private Map<String, Object> fragmentParameters;
    
    private RenderedCriterion(String whereClauseFragment, Map<String, Object> fragmentParameters) {
        this.whereClauseFragment = whereClauseFragment;
        this.fragmentParameters = Collections.unmodifiableMap(new HashMap<>(fragmentParameters));
    }

    public String whereClauseFragment() {
        return whereClauseFragment;
    }

    public Map<String, Object> fragmentParameters() {
        return fragmentParameters;
    }
    
    public static RenderedCriterion of(String whereClauseFragment, Map<String, Object> fragmentParameters) {
        return new RenderedCriterion(whereClauseFragment, fragmentParameters);
    }
}
