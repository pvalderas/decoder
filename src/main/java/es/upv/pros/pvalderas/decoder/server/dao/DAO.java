package es.upv.pros.pvalderas.decoder.server.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import es.upv.pros.pvalderas.decoder.bpmn.domain.BPMNProcess;

@Component
public class DAO {

	 @Autowired
	 private JdbcTemplate jdbcTemplate;
	 
	 public void saveProcess(BPMNProcess fragment, String fileName){
		 jdbcTemplate.update("DELETE FROM processes WHERE id=?", fragment.getId());
		 jdbcTemplate.update("INSERT INTO processes (id, name, file) VALUES (?,?,?)", 
				 				fragment.getId(), fragment.getName(), fileName);
	 }
	 
	
	 public List<Map<String, Object>> getProcesses(){
		 return jdbcTemplate.queryForList("SELECT * FROM processes");
	 }
	 
	 public Map<String, Object> getProcess(String id){
		 return jdbcTemplate.queryForMap("SELECT * FROM processes WHERE id=?",id);
	 }
	 
	 public void createProcessTable(){
	    	jdbcTemplate.execute("DROP TABLE processes IF EXISTS");
	        jdbcTemplate.execute("CREATE TABLE processes(id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), file VARCHAR(1024))");         
	 }
	 
}
