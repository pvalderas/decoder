package es.upv.pros.pvalderas.decoder.server.bpmn;

import java.io.IOException;
import java.util.Properties;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import es.upv.pros.pvalderas.decoder.http.HTTPClient;

@Component
public class ServiceClass implements JavaDelegate
{
	FixedValue toolID;
	FixedValue artifactID;
    
    public void execute(final DelegateExecution execution) throws IOException, JSONException {
    	
    	YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
		
        String data=HTTPClient.get(props.getProperty("decoder.pkmURL")+"/"+toolID.getExpressionText());
        JSONObject executionData=new JSONObject(data);
        
        String server=executionData.getJSONObject("openApiSpecification").getJSONArray("servers").getJSONObject(0).getString("url");
        String path=(String)executionData.getJSONObject("openApiSpecification").getJSONObject("paths").keys().next();
        path=path.replace("{artefactID}", artifactID.getExpressionText());

    	String results=HTTPClient.get(server+path);
    	
    	
    	System.out.println("***********************************************************");
    	System.out.println("TASK EXECUTION: "+executionData.getString("toolID"));
        System.out.println("Artifact ID:"+artifactID.getExpressionText());
       
        System.out.println("Results");
        System.out.println("---------");
        System.out.println(results);
        
        System.out.println("***********************************************************");

      
        
    }
}