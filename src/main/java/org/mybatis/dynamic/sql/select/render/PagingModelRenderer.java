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

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class PagingModelRenderer {
    private RenderingStrategy renderingStrategy;
    private PagingModel pagingModel;

    private PagingModelRenderer(Builder builder) {
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        pagingModel = Objects.requireNonNull(builder.pagingModel);
    }
    
    public Optional<FragmentAndParameters> render() {
        return pagingModel.limit().map(this::limitAndOffsetRender)
                .orElseGet(this::fetchFirstRender);
    }
    
    private Optional<FragmentAndParameters> limitAndOffsetRender(Long limit) {
        return new LimitAndOffsetPagingModelRenderer(renderingStrategy, limit,
                pagingModel).render();
    }
    
    private Optional<FragmentAndParameters> fetchFirstRender() {
        return new FetchFirstPagingModelRenderer(renderingStrategy, pagingModel).render();
    }
    
    public static class Builder {
        private RenderingStrategy renderingStrategy;
        private PagingModel pagingModel;
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withPagingModel(PagingModel pagingModel) {
            this.pagingModel = pagingModel;
            return this;
        }
        
        public PagingModelRenderer build() {
            return new PagingModelRenderer(this);
        }
    }
}
