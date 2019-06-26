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
package org.mybatis.dynamic.sql.select;

import java.util.Optional;

public class FetchFirstPagingModel implements PagingModel {

    private Long fetchFirstRows;
    private Long offset;
    
    private FetchFirstPagingModel(Builder builder) {
        this.fetchFirstRows = builder.fetchFirstRows;
        this.offset = builder.offset;
    }
    
    public Optional<Long> fetchFirstRows() {
        return Optional.ofNullable(fetchFirstRows);
    }
    
    public Optional<Long> offset() {
        return Optional.ofNullable(offset);
    }
    
    @Override
    public <R> R accept(PagingModelVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static class Builder {
        private Long fetchFirstRows;
        private Long offset;

        public Builder withFetchFirstRows(Long fetchFirstRows) {
            this.fetchFirstRows = fetchFirstRows;
            return this;
        }
        
        public Builder withOffset(Long offset) {
            this.offset = offset;
            return this;
        }
        
        public FetchFirstPagingModel build() {
            return new FetchFirstPagingModel(this);
        }
    }
}
