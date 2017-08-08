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
package org.mybatis.dynamic.sql.delete.render;

import java.util.Collections;

import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class DeleteRenderer {
    private DeleteModel deleteModel;
    
    private DeleteRenderer(DeleteModel deleteModel) {
        this.deleteModel = deleteModel;
    }
    
    public DeleteSupport render(RenderingStrategy renderingStrategy) {
        return deleteModel.whereModel().map(wm -> renderWithWhereClause(wm, renderingStrategy))
                .orElse(DeleteSupport.of(deleteModel.table()));
    }
    
    private DeleteSupport renderWithWhereClause(WhereModel whereModel, RenderingStrategy renderingStrategy) {
        WhereRenderer whereRenderer = WhereRenderer.of(whereModel, renderingStrategy, Collections.emptyMap());
        WhereSupport whereSupport = whereRenderer.render();
        return DeleteSupport.of(whereSupport.getWhereClause(), whereSupport.getParameters(), deleteModel.table());
    }
    
    public static DeleteRenderer of(DeleteModel deleteModel) {
        return new DeleteRenderer(deleteModel);
    }
}
