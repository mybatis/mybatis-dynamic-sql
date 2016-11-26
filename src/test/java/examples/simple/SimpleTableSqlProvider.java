package examples.simple;

import org.mybatis.qbe.sql.where.WhereSupport;

public class SimpleTableSqlProvider {
    
    public String selectByExample(WhereSupport whereSupport) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("select a.id, a.first_name as firstName, a.last_name as lastName, a.birth_date as birthDate, a.occupation ");
        sb.append("from SimpleTable a ");
        sb.append(whereSupport.getWhereClause());
        
        return sb.toString();
    }

    public String deleteByExample(WhereSupport whereSupport) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("delete from SimpleTable ");
        sb.append(whereSupport.getWhereClause());
        
        return sb.toString();
    }
}
