/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.insalyon.creatis.vip.query.client.bean;

import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 *
 * @author Boujelben
 */
import java.sql.Date;
import java.sql.Timestamp;
public class QueryRecord extends ListGridRecord {

    public QueryRecord(String name,String dateCreation,String version,String queryversionID ) {

        setAttribute("name", name);
        setAttribute("dateCreation", dateCreation);
        setAttribute("version", version);
      setAttribute("queryversionID", queryversionID);
       
        
    }
}
    
