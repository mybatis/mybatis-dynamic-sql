/*
 *    Copyright 2016-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.render;

import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteRenderer;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.InsertSelectModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.BatchInsertRenderer;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertRenderer;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertRenderer;
import org.mybatis.dynamic.sql.insert.render.InsertSelectRenderer;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertRenderer;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.MultiSelectModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.MultiSelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateRenderer;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public interface RendererFactory {
    static Renderer<RenderingStrategy, DeleteStatementProvider> createDeleteRenderer(DeleteModel deleteModel) {
        return renderingStrategy -> DeleteRenderer.withDeleteModel(deleteModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    static <T> Renderer<RenderingStrategy, BatchInsert<T>> createBatchInsertRenderer(
            BatchInsertModel<T> batchInsertModel) {
        return renderingStrategy -> BatchInsertRenderer.withBatchInsertModel(batchInsertModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    static Renderer<RenderingStrategy, GeneralInsertStatementProvider> createGeneralInsertRenderer(
            GeneralInsertModel generalInsertModel) {
        return renderingStrategy -> GeneralInsertRenderer.withInsertModel(generalInsertModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    static <T> Renderer<RenderingStrategy, InsertStatementProvider<T>> createInsertRenderer(
            InsertModel<T> insertModel) {
        return renderingStrategy -> InsertRenderer.withInsertModel(insertModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    static Renderer<RenderingStrategy, InsertSelectStatementProvider> createInsertSelectRenderer(
            InsertSelectModel insertSelectModel) {
        return renderingStrategy -> InsertSelectRenderer.withInsertSelectModel(insertSelectModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    static <T> Renderer<RenderingStrategy, MultiRowInsertStatementProvider<T>> createMultiRowInsertRenderer(
            MultiRowInsertModel<T> multiRowInsertModel) {
        return renderingStrategy -> MultiRowInsertRenderer.withMultiRowInsertModel(multiRowInsertModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    static Renderer<RenderingStrategy, SelectStatementProvider> createMultiSelectRenderer(
            MultiSelectModel multiSelectModel) {
        return renderingStrategy -> new MultiSelectRenderer.Builder()
                .withMultiSelectModel(multiSelectModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    static Renderer<RenderingContext, FragmentAndParameters> createSelectRenderer(SelectModel selectModel) {
        return renderingContext -> SelectRenderer.withSelectModel(selectModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    static Renderer<RenderingStrategy, UpdateStatementProvider> createUpdateRenderer(UpdateModel updateModel) {
        return renderingStrategy -> UpdateRenderer.withUpdateModel(updateModel)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }
}
