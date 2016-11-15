package animal.data;

import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class AnimalDataSqlProvider {

    public String selectByExample(WhereClauseAndParameters whereClauseAndParameters) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("select a.id, a.animal_name as animalName, a.brain_weight as brainWeight, a.body_weight as bodyWeight");
        buffer.append(" from AnimalData a");
        if (whereClauseAndParameters != null) {
            buffer.append(' ');
            buffer.append(whereClauseAndParameters.getWhereClause());
        }
        buffer.append(" order by a.id");
        
        return buffer.toString();
    }

    public String deleteByExample(WhereClauseAndParameters whereClauseAndParameters) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("delete from AnimalData");
        if (whereClauseAndParameters != null) {
            buffer.append(' ');
            buffer.append(whereClauseAndParameters.getWhereClause());
        }
        
        return buffer.toString();
    }
}
