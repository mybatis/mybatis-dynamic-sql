/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.NotCriterion;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlCriterionVisitor;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.render.SubQueryRenderer;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

/**
 * Renders a {@link SqlCriterion} to a {@link RenderedCriterion}. The process is complex because all conditions
 * may or may not be a candidate for rendering. For example, "isEqualWhenPresent" will not render when the value
 * is null. It is also complex because SqlCriterion may or may not include sub-criteria.
 *
 * <p>Rendering is a recursive process. The renderer will recurse into each sub-criteria - which may also
 * contain further sub-criteria - until all possible sub-criteria are rendered into a single fragment. So, for example,
 * the fragment may end up looking like:
 *
 * <pre>
 *     col1 = ? and (col2 = ? or (col3 = ? and col4 = ?))
 * </pre>
 *
 * <p>It is also possible that the end result will be empty if all criteria and sub-criteria are not valid for
 * rendering.
 *
 * @author Jeff Butler
 */
public class CriterionRenderer implements SqlCriterionVisitor<Optional<RenderedCriterion>> {
    private final RenderingContext renderingContext;

    public CriterionRenderer(RenderingContext renderingContext) {
        this.renderingContext = Objects.requireNonNull(renderingContext);
    }

    @Override
    public <T> Optional<RenderedCriterion> visit(ColumnAndConditionCriterion<T> criterion) {
        Optional<FragmentAndParameters> initialCriterion = renderColumnAndCondition(criterion);
        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(criterion.subCriteria());

        return initialCriterion.map(fp -> calculateRenderedCriterion(fp, renderedSubCriteria, this::calculateFragment))
                .orElseGet(() -> calculateRenderedCriterion(renderedSubCriteria, this::calculateFragment));
    }

    @Override
    public Optional<RenderedCriterion> visit(ExistsCriterion criterion) {
        FragmentAndParameters initialCriterion = renderExists(criterion);
        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(criterion.subCriteria());

        return calculateRenderedCriterion(initialCriterion, renderedSubCriteria, this::calculateFragment);
    }

    @Override
    public Optional<RenderedCriterion> visit(CriteriaGroup criterion) {
        return renderCriteriaGroup(criterion, this::calculateFragment);
    }

    @Override
    public Optional<RenderedCriterion> visit(NotCriterion criterion) {
        return renderCriteriaGroup(criterion, this::calculateNotFragment);
    }

    private Optional<RenderedCriterion> renderCriteriaGroup(CriteriaGroup criterion,
                                                            Function<FragmentCollector, String> fragmentCalculator) {
        return criterion.initialCriterion().map(ic -> render(ic, criterion.subCriteria(), fragmentCalculator))
                .orElseGet(() -> render(criterion.subCriteria(), fragmentCalculator));
    }

    public Optional<RenderedCriterion> render(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria,
                                              Function<FragmentCollector, String> fragmentCalculator) {
        Optional<FragmentAndParameters> fragmentAndParameters = initialCriterion.accept(this)
                .map(RenderedCriterion::fragmentAndParameters);
        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(subCriteria);

        return fragmentAndParameters.map(fp -> calculateRenderedCriterion(fp, renderedSubCriteria, fragmentCalculator))
                .orElseGet(() -> calculateRenderedCriterion(renderedSubCriteria, fragmentCalculator));
    }

    public Optional<RenderedCriterion> render(List<AndOrCriteriaGroup> subCriteria,
                                              Function<FragmentCollector, String> fragmentCalculator) {
        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(subCriteria);
        return calculateRenderedCriterion(renderedSubCriteria, fragmentCalculator);
    }

    private <T> Optional<FragmentAndParameters> renderColumnAndCondition(ColumnAndConditionCriterion<T> criterion) {
        if (criterion.condition().shouldRender(renderingContext)) {
            return Optional.of(renderCondition(criterion));
        } else {
            criterion.condition().renderingSkipped();
            return Optional.empty();
        }
    }

    private FragmentAndParameters renderExists(ExistsCriterion criterion) {
        ExistsPredicate existsPredicate = criterion.existsPredicate();
        return SubQueryRenderer.withSelectModel(existsPredicate.selectModelBuilder().build())
                .withRenderingContext(renderingContext)
                .withPrefix(existsPredicate.operator() + " (") //$NON-NLS-1$
                .withSuffix(")") //$NON-NLS-1$
                .build()
                .render();
    }

    private List<RenderedCriterion> renderSubCriteria(List<AndOrCriteriaGroup> subCriteria) {
        return subCriteria.stream().map(this::renderAndOrCriteriaGroup)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<RenderedCriterion> renderAndOrCriteriaGroup(AndOrCriteriaGroup criterion) {
        return criterion.initialCriterion().map(ic -> render(ic, criterion.subCriteria(), this::calculateFragment))
                .orElseGet(() -> render(criterion.subCriteria(), this::calculateFragment))
                .map(rc -> rc.withConnector(criterion.connector()));
    }

    private Optional<RenderedCriterion> calculateRenderedCriterion(FragmentAndParameters initialCriterion,
            List<RenderedCriterion> renderedSubCriteria, Function<FragmentCollector, String> fragmentCalculator) {
        return Optional.of(calculateRenderedCriterion(
                collectSqlFragments(initialCriterion, renderedSubCriteria), fragmentCalculator));
    }

    private RenderedCriterion calculateRenderedCriterion(FragmentCollector fragmentCollector,
                                                         Function<FragmentCollector, String> fragmentCalculator) {
        FragmentAndParameters fragmentAndParameters = FragmentAndParameters
                .withFragment(fragmentCalculator.apply(fragmentCollector))
                .withParameters(fragmentCollector.parameters())
                .build();

        return new RenderedCriterion.Builder()
                .withFragmentAndParameters(fragmentAndParameters)
                .build();
    }

    private Optional<RenderedCriterion> calculateRenderedCriterion(List<RenderedCriterion> renderedSubCriteria,
            Function<FragmentCollector, String> fragmentCalculator) {
        return collectSqlFragments(renderedSubCriteria).map(fc -> calculateRenderedCriterion(fc, fragmentCalculator));
    }

    private <T> FragmentAndParameters renderCondition(ColumnAndConditionCriterion<T> criterion) {
        return new ColumnAndConditionRenderer.Builder<T>()
                .withColumn(criterion.column())
                .withCondition(criterion.condition())
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    /**
     * This method encapsulates the logic of building a collection of fragments from an initial condition
     * and a list of rendered sub criteria. In this overload we know there is an initial condition
     * and there may be subcriteria. The collector will contain the initial condition and any rendered subcriteria
     * in order.
     *
     * @param initialCondition - may not be null. If there is no initial condition, then use the other overload
     * @param renderedSubCriteria - a list of previously rendered sub criteria. The sub criteria will all
     *                            have connectors (either an AND or an OR)
     * @return a fragment collector whose fragments represent the final calculated list of fragments and parameters.
     *     The fragment collector can be used to calculate the single composed fragment - either as a where clause, or
     *     a valid rendered sub criteria in the case of a recursive call.
     */
    private FragmentCollector collectSqlFragments(FragmentAndParameters initialCondition,
                                                  List<RenderedCriterion> renderedSubCriteria) {
        return renderedSubCriteria.stream()
                .map(RenderedCriterion::fragmentAndParametersWithConnector)
                .collect(FragmentCollector.collect(initialCondition));
    }

    /**
     * This method encapsulates the logic of building a collection of fragments from a list of rendered sub criteria.
     * In this overload we take the initial condition to be the first element in the subcriteria list.
     * The collector will contain the rendered subcriteria in order. However, the connector from the first rendered
     * sub criterion will be removed. This to avoid generating an invalid where clause like "where and a < 3"
     *
     * @param renderedSubCriteria - a list of previously rendered sub criteria. The sub criteria will all
     *                            have connectors (either an AND or an OR)
     * @return a fragment collector whose fragments represent the final calculated list of fragments and parameters.
     *     The fragment collector can be used to calculate the single composed fragment - either as a where clause, or
     *     a valid rendered sub criteria in the case of a recursive call.
     */
    private Optional<FragmentCollector> collectSqlFragments(List<RenderedCriterion> renderedSubCriteria) {
        if (renderedSubCriteria.isEmpty()) {
            return Optional.empty();
        }

        FragmentAndParameters firstCondition = renderedSubCriteria.get(0).fragmentAndParameters();

        FragmentCollector fc = renderedSubCriteria.stream()
                .skip(1)
                .map(RenderedCriterion::fragmentAndParametersWithConnector)
                .collect(FragmentCollector.collect(firstCondition));

        return Optional.of(fc);
    }

    private String calculateFragment(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.collectFragments(
                    Collectors.joining(" ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.firstFragment().orElse(""); //$NON-NLS-1$
        }
    }

    private String calculateNotFragment(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.collectFragments(
                    Collectors.joining(" ", "not (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.firstFragment().map(s -> "not " + s).orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
