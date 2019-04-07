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
package org.mybatis.dynamic.sql.where.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class RenderedCriterion {
    private Optional<String> connector;
    private Optional<FragmentAndParameters> initialCondition;
    private List<RenderedCriterion> subCriteria;
    
    private RenderedCriterion(Builder builder) {
        connector = Objects.requireNonNull(builder.connector);
        initialCondition = Objects.requireNonNull(builder.initialCondition);
        subCriteria = Objects.requireNonNull(builder.subCriteria);
    }

    public FragmentAndParameters renderWithInitialConnector() {
        FragmentAndParameters fp = renderWithoutInitialConnector();
        
        return connector.map(fp::prependFragment).orElse(fp);
    }
    
    public FragmentAndParameters renderWithoutInitialConnector() {
        FragmentCollector fc = internalRender();

        String fragment = calculateFragment(fc);
        
        return FragmentAndParameters.withFragment(fragment)
                .withParameters(fc.parameters())
                .build();
    }

    private FragmentCollector internalRender() {
        return initialCondition.map(this::renderConditionAndSubCriteria)
                .orElseGet(this::renderSubCriteriaOnly);
    }

    private FragmentCollector renderSubCriteriaOnly() {
        FragmentAndParameters initial = subCriteria.get(0).renderWithoutInitialConnector();
        
        return subCriteria.stream()
                .skip(1)
                .map(RenderedCriterion::renderWithInitialConnector)
                .collect(FragmentCollector.collect(initial));
    }

    private FragmentCollector renderConditionAndSubCriteria(FragmentAndParameters initialCondition) {
        return subCriteria.stream()
                .map(RenderedCriterion::renderWithInitialConnector)
                .collect(FragmentCollector.collect(initialCondition));
    }

    private String calculateFragment(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.fragments()
                    .collect(Collectors.joining(" ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.fragments().findFirst().orElse(""); //$NON-NLS-1$
        }
    }
    
    public static class Builder {
        private Optional<String> connector = Optional.empty();
        private Optional<FragmentAndParameters> initialCondition = Optional.empty();
        private List<RenderedCriterion> subCriteria = new ArrayList<>();

        public Builder withConnector(Optional<String> connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder withInitialCondition(Optional<FragmentAndParameters> initialCondition) {
            this.initialCondition = initialCondition;
            return this;
        }
        
        public Builder withSubCriteria(List<RenderedCriterion> subCriteria) {
            this.subCriteria.addAll(subCriteria);
            return this;
        }
        
        public Optional<RenderedCriterion> build() {
            if (!initialCondition.isPresent() && subCriteria.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new RenderedCriterion(this));
        }
    }
}
