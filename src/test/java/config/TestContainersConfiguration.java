/*
 *    Copyright 2016-2023 the original author or authors.
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
package config;

import org.testcontainers.utility.DockerImageName;

/**
 * Utility interface to hold Docker image tags for the test containers we use
 */
public interface TestContainersConfiguration {
    DockerImageName POSTGRES_LATEST = DockerImageName.parse("postgres:15.1");
    DockerImageName MARIADB_LATEST = DockerImageName.parse("mariadb:10.10.2");
}
