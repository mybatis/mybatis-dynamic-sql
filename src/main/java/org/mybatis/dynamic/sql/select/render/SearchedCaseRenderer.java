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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.Messages;

public class SearchedCaseRenderer {
    private final SearchedCaseModel searchedCaseModel;
    private final RenderingContext renderingContext;

    public SearchedCaseRenderer(SearchedCaseModel searchedCaseModel, RenderingContext renderingContext) {
        this.searchedCaseModel = Objects.requireNonNull(searchedCaseModel);
        this.renderingContext = Objects.requireNonNull(renderingContext);
    }

    public FragmentAndParameters render() {
        return Stream.of(
                renderCase(),
                renderWhenConditions(),
                renderElse(),
                renderEnd()
        )
                .flatMap(Function.identity())
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private Stream<FragmentAndParameters> renderCase() {
        return Stream.of(FragmentAndParameters.fromFragment("case")); //$NON-NLS-1$
    }

    private Stream<FragmentAndParameters> renderWhenConditions() {
        return searchedCaseModel.whenConditions().flatMap(this::renderWhenCondition);
    }

    private Stream<FragmentAndParameters> renderWhenCondition(SearchedCaseModel.SearchedWhenCondition whenCondition) {
        return Stream.of(renderWhen(whenCondition), renderThen(whenCondition));
    }

    private FragmentAndParameters renderWhen(SearchedCaseModel.SearchedWhenCondition whenCondition) {
        SearchedCaseWhenConditionRenderer renderer = new SearchedCaseWhenConditionRenderer.Builder(whenCondition)
                .withRenderingContext(renderingContext)
                .build();

        return renderer.render()
                .orElseThrow(() -> new InvalidSqlException(Messages.getString("ERROR.39"))); //$NON-NLS-1$
    }

    private FragmentAndParameters renderThen(SearchedCaseModel.SearchedWhenCondition whenCondition) {
        return FragmentAndParameters.fromFragment("then " + whenCondition.thenValue()); //$NON-NLS-1$
    }

    private Stream<FragmentAndParameters> renderElse() {
        return searchedCaseModel.elseValue().map(this::renderElse).map(Stream::of).orElseGet(Stream::empty);
    }

    private FragmentAndParameters renderElse(Object elseValue) {
        return FragmentAndParameters.fromFragment("else " + elseValue); //$NON-NLS-1$
    }

    private Stream<FragmentAndParameters> renderEnd() {
        return Stream.of(FragmentAndParameters.fromFragment("end")); //$NON-NLS-1$
    }
}
