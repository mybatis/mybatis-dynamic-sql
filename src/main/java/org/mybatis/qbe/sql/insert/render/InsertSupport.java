package org.mybatis.qbe.sql.insert.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InsertSupport {

    private String fieldsPhrase;
    private String valuesPhrase;
    private Map<String, Object> parameters;
    
    private InsertSupport(String fieldsPhrase, String valuesPhrase, Map<String, Object> parameters) {
        this.fieldsPhrase = fieldsPhrase;
        this.valuesPhrase = valuesPhrase;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }
    
    public String getFieldsPhrase() {
        return fieldsPhrase;
    }

    public String getValuesPhrase() {
        return valuesPhrase;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static InsertSupport of(String fieldsPhrase, String valuesPhrase, Map<String, Object> parameters) {
        return new InsertSupport(fieldsPhrase, valuesPhrase, parameters);
    }
}
