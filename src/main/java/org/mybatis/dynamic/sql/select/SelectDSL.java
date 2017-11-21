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

import java.util.ArrayList;
import java.util.List;

import org.mybatis.dynamic.sql.BasicColumn;

public class SelectDSL {

    private List<QueryExpressionModel> queryExpressions = new ArrayList<>();    
    private OrderByModel orderByModel;
    
    private SelectDSL() {
        super();
    }

    private QueryExpressionDSL queryExpressionBuilder(BasicColumn...selectList) {
        return new QueryExpressionDSL.Builder()
                .withSelectList(selectList)
                .withSelectModelBuilder(this)
                .build();
    }
    
    private QueryExpressionDSL distinctQueryExpressionBuilder(BasicColumn...selectList) {
        return new QueryExpressionDSL.Builder()
                .withSelectList(selectList)
                .isDistinct()
                .withSelectModelBuilder(this)
                .build();
    }
    
    public static QueryExpressionDSL select(BasicColumn...selectList) {
        SelectDSL selectModelBuilder = new SelectDSL();
        return selectModelBuilder.queryExpressionBuilder(selectList);
    }
    
    public static QueryExpressionDSL selectDistinct(BasicColumn...selectList) {
        SelectDSL selectModelBuilder = new SelectDSL();
        return selectModelBuilder.distinctQueryExpressionBuilder(selectList);
    }
    
    void addQueryExpression(QueryExpressionModel queryExpression) {
        queryExpressions.add(queryExpression);
    }
    
    void setOrderByModel(OrderByModel orderByModel) {
        this.orderByModel = orderByModel;
    }
    
    public SelectModel build() {
        return new SelectModel.Builder()
                .withQueryExpressions(queryExpressions)
                .withOrderByModel(orderByModel)
                .build();
    }
}
