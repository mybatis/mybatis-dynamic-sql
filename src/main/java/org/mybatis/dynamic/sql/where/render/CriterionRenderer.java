/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.NotCriterion;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlCriterionVisitor;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
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
    private final AtomicInteger sequence;
    private final RenderingStrategy renderingStrategy;
    private final TableAliasCalculator tableAliasCalculator;
    private final String parameterName;

    private CriterionRenderer(Builder builder) {
        sequence = Objects.requireNonNull(builder.sequence);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        parameterName = builder.parameterName;
    }

    @Override
    public <T> Optional<RenderedCriterion> visit(ColumnAndConditionCriterion<T> criterion) {
        FragmentAndParameters fp = null;
        if (criterion.condition().shouldRender()) {
            fp = renderCondition(criterion);
        } else {
            criterion.condition().renderingSkipped();
        }

        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(criterion.subCriteria());

        Optional<FragmentCollector> fragmentCollector = collectSqlFragments(fp, renderedSubCriteria);
        return fragmentCollector.map(fc -> {
            FragmentAndParameters fp1 = FragmentAndParameters.withFragment(calculateFragment(fc))
                    .withParameters(fc.parameters())
                    .build();

            return new RenderedCriterion.Builder()
                    .withFragmentAndParameters(fp1)
                    .build();

        });
    }

    @Override
    public Optional<RenderedCriterion> visit(ExistsCriterion criterion) {
        ExistsPredicate existsPredicate = criterion.existsPredicate();

        SelectStatementProvider selectStatement = SelectRenderer
                .withSelectModel(existsPredicate.selectModelBuilder().build())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();

        String fragment = existsPredicate.operator()
                + " (" //$NON-NLS-1$
                + selectStatement.getSelectStatement()
                + ")"; //$NON-NLS-1$

        FragmentAndParameters initialCondition = FragmentAndParameters
                .withFragment(fragment)
                .withParameters(selectStatement.getParameters())
                .build();

        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(criterion.subCriteria());

        Optional<FragmentCollector> fragmentCollector = collectSqlFragments(initialCondition, renderedSubCriteria);
        return fragmentCollector.map(fc -> {
            FragmentAndParameters fp1 = FragmentAndParameters.withFragment(calculateFragment(fc))
                    .withParameters(fc.parameters())
                    .build();

            return new RenderedCriterion.Builder()
                    .withFragmentAndParameters(fp1)
                    .build();

        });
    }

    @Override
    public Optional<RenderedCriterion> visit(CriteriaGroup criterion) {
        Optional<RenderedCriterion> initialCriterion = criterion.initialCriterion().accept(this);
        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(criterion.subCriteria());

        Optional<FragmentCollector> fragmentCollector = collectSqlFragments(initialCriterion.map(RenderedCriterion::fragmentAndParameters)
                .orElse(null), renderedSubCriteria);
        return fragmentCollector.map(fc -> {
            FragmentAndParameters fp1 = FragmentAndParameters.withFragment(calculateFragment(fc))
                    .withParameters(fc.parameters())
                    .build();

            return new RenderedCriterion.Builder()
                    .withFragmentAndParameters(fp1)
                    .build();

        });
    }

    @Override
    public Optional<RenderedCriterion> visit(NotCriterion criterion) {
        Optional<RenderedCriterion> initialCriterion = criterion.initialCriterion().accept(this);
        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(criterion.subCriteria());

        Optional<FragmentCollector> fragmentCollector = collectSqlFragments(initialCriterion.map(RenderedCriterion::fragmentAndParameters)
                .orElse(null), renderedSubCriteria);
        return fragmentCollector.map(fc -> {
            FragmentAndParameters fp1 = FragmentAndParameters.withFragment(calculateNotFragment(fc))
                    .withParameters(fc.parameters())
                    .build();

            return new RenderedCriterion.Builder()
                    .withFragmentAndParameters(fp1)
                    .build();

        });
    }

    List<RenderedCriterion> renderSubCriteria(List<AndOrCriteriaGroup> subCriteria) {
        return subCriteria.stream().map(this::renderAndOrCriteriaGroup)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<RenderedCriterion> renderAndOrCriteriaGroup(AndOrCriteriaGroup criterion) {
        Optional<RenderedCriterion> initialCriterion = criterion.initialCriterion().accept(this);
        List<RenderedCriterion> renderedSubCriteria = renderSubCriteria(criterion.subCriteria());

        Optional<FragmentCollector> fragmentCollector = collectSqlFragments(initialCriterion.map(RenderedCriterion::fragmentAndParameters)
                .orElse(null), renderedSubCriteria);
        return fragmentCollector.map(fc -> {
            FragmentAndParameters fp1 = FragmentAndParameters.withFragment(calculateFragment(fc))
                    .withParameters(fc.parameters())
                    .build();

            return new RenderedCriterion.Builder()
                    .withFragmentAndParameters(fp1)
                    .build();
        }).map(rc -> rc.withConnector(criterion.connector()));
    }

    private <T> FragmentAndParameters renderCondition(ColumnAndConditionCriterion<T> criterion) {
        WhereConditionVisitor<T> visitor = WhereConditionVisitor.withColumn(criterion.column())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withTableAliasCalculator(tableAliasCalculator)
                .withParameterName(parameterName)
                .build();
        return criterion.condition().accept(visitor);
    }

    /**
     * This method encapsulates the logic of building a collection of fragments from an initial condition
     * and a list of rendered sub criteria. There are three cases:
     *
     * <ol>
     *     <li>If there is no initial condition and there are no rendered sub criteria, then there will
     *     be no rendered criteria and this set of criteria should be skipped altogether</li>
     *     <li>If there are both and initial condition and rendered sub criteria, then the final set will
     *     include all fragments in order</li>
     *     <li>If there is no initial fragment, but there are rendered sub criteria, then the final set
     *     will include just the sub criteria. However, the connector from the first rendered sub criterion
     *     will be removed. This to avoid generating an invalid where clause like "where and a < 3"</li>
     * </ol>
     *
     * @param initialCondition - may be null if there is no rendered initial condition (as can be the case of
     *                         optional conditions like isEqualToWhenPresent)
     * @param renderedSubCriteria - a list of previously rendered sub criteria. The sub criteria will all
     *                            have connectors (either an AND or an OR)
     * @return a fragment collector whose fragments represent the final calculated list of fragments and parameters.
     *     The fragment collector can be used to calculate the single composed fragment - either as a where clause, or
     *     a valid rendered sub criteria in the case of a recursive call.
     */
    Optional<FragmentCollector> collectSqlFragments(FragmentAndParameters initialCondition,
                                                    List<RenderedCriterion> renderedSubCriteria) {
        if (initialCondition == null && renderedSubCriteria.isEmpty()) {
            return Optional.empty();
        }

        int skip = 0;
        FragmentAndParameters firstCondition = initialCondition;
        if (firstCondition == null) {
            firstCondition = renderedSubCriteria.get(0).fragmentAndParameters();
            skip = 1;
        }

        FragmentCollector fc = renderedSubCriteria.stream()
                .skip(skip)
                .map(RenderedCriterion::fragmentAndParametersWithConnector)
                .collect(FragmentCollector.collect(firstCondition));

        return Optional.of(fc);
    }

    private String calculateFragment(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.fragments()
                    .collect(Collectors.joining(" ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.fragments().findFirst().orElse(""); //$NON-NLS-1$
        }
    }

    private String calculateNotFragment(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.fragments()
                    .collect(Collectors.joining(" ", "not (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.fragments().findFirst().map(s -> "not " + s).orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static class Builder {
        private AtomicInteger sequence;
        private RenderingStrategy renderingStrategy;
        private TableAliasCalculator tableAliasCalculator;
        private String parameterName;

        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public Builder withParameterName(String parameterName) {
            this.parameterName = parameterName;
            return this;
        }

        public CriterionRenderer build() {
            return new CriterionRenderer(this);
        }
    }
}
