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
package org.mybatis.dynamic.sql.select.render;

import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.FetchFirstPagingModel;
import org.mybatis.dynamic.sql.select.LimitAndOffsetPagingModel;
import org.mybatis.dynamic.sql.select.PagingModelVisitor;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class PagingModelRenderer implements PagingModelVisitor<Optional<FragmentAndParameters>> {
    private RenderingStrategy renderingStrategy;

    public PagingModelRenderer(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
    }
    
    @Override
    public Optional<FragmentAndParameters> visit(LimitAndOffsetPagingModel pagingModel) {
        return new LimitAndOffsetPagingModelRenderer(renderingStrategy, pagingModel).render();
    }

    @Override
    public Optional<FragmentAndParameters> visit(FetchFirstPagingModel pagingModel) {
        return new FetchFirstPagingModelRenderer(renderingStrategy, pagingModel).render();
    }
}
