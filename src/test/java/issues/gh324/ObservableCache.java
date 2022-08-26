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
package issues.gh324;

import org.apache.ibatis.cache.impl.PerpetualCache;

public class ObservableCache extends PerpetualCache {

    private static ObservableCache instance;

    public static ObservableCache getInstance() {
        return instance;
    }

    private int requests = 0;

    public int getRequests() {
        return requests;
    }

    public int getHits() {
        return hits;
    }

    private int hits = 0;

    public ObservableCache(String id) {
        super(id);
        instance = this;
    }

    @Override
    public void putObject(Object key, Object value) {
        super.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        Object answer = super.getObject(key);

        if (key.toString().contains("select id, name from NameTable where id = ?")) {
            requests++;

            if (answer != null) {
                hits++;
            }
        }

        return answer;
    }

    @Override
    public void clear() {
        requests = 0;
        hits = 0;
        super.clear();
    }
}
