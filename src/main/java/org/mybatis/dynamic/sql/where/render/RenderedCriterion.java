/*
 *    Copyright 2016-2022 the original author or authors.
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

import java.util.Objects;

import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class RenderedCriterion {
    private final String connector;
    private final FragmentAndParameters fragmentAndParameters;

    private RenderedCriterion(Builder builder) {
        connector = builder.connector;
        fragmentAndParameters = Objects.requireNonNull(builder.fragmentAndParameters);
    }

    public FragmentAndParameters fragmentAndParameters() {
        return fragmentAndParameters;
    }

    public FragmentAndParameters fragmentAndParametersWithConnector() {
        if (connector == null) {
            return fragmentAndParameters;
        } else {
            return prependFragment(fragmentAndParameters, connector);
        }
    }

    public RenderedCriterion withConnector(String connector) {
        return new RenderedCriterion.Builder()
                .withFragmentAndParameters(fragmentAndParameters)
                .withConnector(connector)
                .build();
    }

    private FragmentAndParameters prependFragment(FragmentAndParameters fragmentAndParameters, String connector) {
        return FragmentAndParameters.withFragment(connector + " " + fragmentAndParameters.fragment()) //$NON-NLS-1$
                .withParameters(fragmentAndParameters.parameters())
                .build();
    }

    public static class Builder {
        private String connector;
        private FragmentAndParameters fragmentAndParameters;

        public Builder withConnector(String connector) {
            this.connector = connector;
            return this;
        }

        public Builder withFragmentAndParameters(FragmentAndParameters fragmentAndParameters) {
            this.fragmentAndParameters = fragmentAndParameters;
            return this;
        }

        public RenderedCriterion build() {
            return new RenderedCriterion(this);
        }
    }
}
