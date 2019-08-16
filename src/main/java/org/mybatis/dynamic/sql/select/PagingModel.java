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

public class PagingModel {

    private Long limit;
    private Long offset;
    private Long fetchFirstRows;

    private PagingModel(Builder builder) {
        super();
        limit = builder.limit;
        offset = builder.offset;
        fetchFirstRows = builder.fetchFirstRows;
    }

    public Optional<Long> limit() {
        return Optional.ofNullable(limit);
    }

    public Optional<Long> offset() {
        return Optional.ofNullable(offset);
    }

    public Optional<Long> fetchFirstRows() {
        return Optional.ofNullable(fetchFirstRows);
    }
    
    public static class Builder {
        private Long limit;
        private Long offset;
        private Long fetchFirstRows;

        public Builder withLimit(Long limit) {
            this.limit = limit;
            return this;
        }
        
        public Builder withOffset(Long offset) {
            this.offset = offset;
            return this;
        }
        
        public Builder withFetchFirstRows(Long fetchFirstRows) {
            this.fetchFirstRows = fetchFirstRows;
            return this;
        }
        
        public PagingModel build() {
            return new PagingModel(this);
        }
    }
}
