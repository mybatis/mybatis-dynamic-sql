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
package examples.springbatch.common;

import static examples.springbatch.mapper.PersonDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UpdateStatementConvertor implements Converter<Person, UpdateStatementProvider> {

    @Override
    public UpdateStatementProvider convert(Person source) {
        return UpdateDSL.update(person)
                .set(firstName).equalTo(source::getFirstName)
                .set(lastName).equalTo(source::getLastName)
                .where(id, isEqualTo(source::getId))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }
}
