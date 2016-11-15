package simple.example;

import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class SimpleTableSqlProvider {
    
    public String selectByExample(WhereClauseAndParameters whereClauseAndParameters) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("select a.id, a.first_name as firstName, a.last_name as lastName, a.birth_date as birthDate, a.occupation ");
        sb.append("from SimpleTable a ");
        sb.append(whereClauseAndParameters.getWhereClause());
        
        return sb.toString();
    }

    public String deleteByExample(WhereClauseAndParameters whereClauseAndParameters) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("delete from SimpleTable ");
        sb.append(whereClauseAndParameters.getWhereClause());
        
        return sb.toString();
    }
}
