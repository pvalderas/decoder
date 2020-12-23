package es.upv.pros.pvalderas.decoder.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import es.upv.pros.pvalderas.decoder.bpmn.domain.BPMNProcess;
import es.upv.pros.pvalderas.decoder.bpmn.utils.XMLQuery;
import es.upv.pros.pvalderas.decoder.server.dao.DAO;

@Component
public class ClientStarter implements ApplicationRunner {
	
	@Autowired
	private DAO dao;
	 
	@Autowired
	private ApplicationContext context;
	
	
	 
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	
		Class mainClass=context.getBeansWithAnnotation(DecoderProcessServer.class).values().iterator().next().getClass().getSuperclass();
		 
		if(mainClass!=null){
			System.out.print("Setting up Decoder Server......");
				        
			this.createProcessFolder();
			dao.createProcessTable(); 
			loadAllProcesses();
				      
			System.out.println("OK");
			
		}
         
    }
    
    
    
    private void createProcessFolder() throws IOException{
    	File dir=new File("processes");
    	dir.mkdirs();
    }
    


    private String[] loadAllProcesses() throws JaxenException, DocumentException, IOException{
		File dir=new File("processes");
    	String processes[]=dir.list();
		
    	if(processes!=null){
        	for(int i=0; i<processes.length;i++){
        		 String processFile=processes[i];
	    		 String name=processFile.substring(0,processFile.indexOf("."));
	    		 BPMNProcess BPMNprocess=new BPMNProcess();
	    		 String xml=new String(Files.readAllBytes(new File("processes/"+processFile).toPath()));
	    		 BPMNprocess.setId(getProcessID(xml));
	    		 BPMNprocess.setName(name);
	    		 dao.saveProcess(BPMNprocess, "processes/"+processFile);
        	}
		}
    	
    	return processes;
    }
    
    private String getProcessID(String xml) throws DocumentException, JaxenException, UnsupportedEncodingException{
    	Document bpmn = DocumentHelper.parseText(new String(xml.getBytes("UTF-8")));
    	
		XMLQuery query=new XMLQuery(bpmn);
		String processQuery = "//bpmn:process";	
		Node process=query.selectSingleNode(processQuery);
		
		String processID=((Element)process).attributeValue("id");
		
		return processID;
    }
    
}
