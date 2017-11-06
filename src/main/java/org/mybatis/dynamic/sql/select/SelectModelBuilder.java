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

import org.mybatis.dynamic.sql.SelectListItem;

public class SelectModelBuilder {

    private List<QueryExpression> queryExpressions = new ArrayList<>();    
    private OrderByModel orderByModel;
    
    private SelectModelBuilder() {
        super();
    }

    private QueryExpressionBuilder queryExpressionBuilder(SelectListItem...selectList) {
        return new QueryExpressionBuilder.Builder()
                .withSelectList(selectList)
                .withSelectModelBuilder(this)
                .build();
    }
    
    private QueryExpressionBuilder distinctQueryExpressionBuilder(SelectListItem...selectList) {
        return new QueryExpressionBuilder.Builder()
                .withSelectList(selectList)
                .isDistinct()
                .withSelectModelBuilder(this)
                .build();
    }
    
    public static QueryExpressionBuilder select(SelectListItem...selectList) {
        SelectModelBuilder selectModelBuilder = new SelectModelBuilder();
        return selectModelBuilder.queryExpressionBuilder(selectList);
    }
    
    public static QueryExpressionBuilder selectDistinct(SelectListItem...selectList) {
        SelectModelBuilder selectModelBuilder = new SelectModelBuilder();
        return selectModelBuilder.distinctQueryExpressionBuilder(selectList);
    }
    
    void addQueryExpression(QueryExpression queryExpression) {
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
