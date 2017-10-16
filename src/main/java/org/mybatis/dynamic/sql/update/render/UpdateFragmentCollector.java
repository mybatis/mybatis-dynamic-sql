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
package org.mybatis.dynamic.sql.update.render;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class UpdateFragmentCollector extends FragmentCollector<UpdateFragmentCollector> {
    
    private SqlTable table;
    private Optional<WhereSupport> whereSupport;
    
    public UpdateFragmentCollector(SqlTable table, Optional<WhereSupport> whereSupport) {
        this.table = table;
        this.whereSupport = whereSupport;
    }
    
    public UpdateSupport buildUpdateSupport() {
        return new UpdateSupport.Builder(table.name())
                .withSetClause(fragments()
                        .collect(Collectors.joining(", ", "set ", ""))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .withWhereSupport(whereSupport)
                .withParameters(parameters)
                .build();
    }
    
    @Override
    public UpdateFragmentCollector getThis() {
        return this;
    }
    
    public static Collector<FragmentAndParameters, UpdateFragmentCollector, UpdateSupport> toUpdateSupport(SqlTable table,
            Optional<WhereSupport> whereSupport) {
        return Collector.of(() -> new UpdateFragmentCollector(table, whereSupport),
                UpdateFragmentCollector::add,
                UpdateFragmentCollector::merge,
                UpdateFragmentCollector::buildUpdateSupport);
    }
}
