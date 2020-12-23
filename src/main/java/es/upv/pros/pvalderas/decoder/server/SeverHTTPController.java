package es.upv.pros.pvalderas.decoder.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.upv.pros.pvalderas.decoder.bpmn.domain.BPMNProcess;
import es.upv.pros.pvalderas.decoder.bpmn.utils.XMLQuery;
import es.upv.pros.pvalderas.decoder.http.HTTPClient;
import es.upv.pros.pvalderas.decoder.server.dao.DAO;

@RestController
@CrossOrigin
public class SeverHTTPController {
	
	@Autowired
	private DAO dao;
	
	@Autowired
	private SpringProcessEngineConfiguration config;
	
	@Autowired
	private ResourcePatternResolver resourceLoader;
	
	@Autowired
	private RuntimeService runtimeService;
	
	
	@RequestMapping(
			  value = "/processes", 
			  method = RequestMethod.POST,
			  consumes = "application/json")
	public void saveBPMNPiece(@RequestBody BPMNProcess process) throws IOException, TimeoutException, JaxenException, DocumentException {
			
			String xml=this.addXMLAtts(process.getXml());
			String fileName=this.createFile(process.getName(), xml);
			this.deployBPMNProcess();
			dao.saveProcess(process, fileName);
	}
	
	@RequestMapping(
			  value = "/operations", 
			  method = RequestMethod.GET,
			  produces = "application/json")
	public String getOperations() throws IOException {
		
			YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
	        yamlFactory.setResources(new ClassPathResource("application.yml"));
	        Properties props=yamlFactory.getObject();
			
			return HTTPClient.get(props.getProperty("decoder.pkmURL")+"?phase=Analysis");
			
	}
	
	private String addXMLAtts(String xml) throws DocumentException, JaxenException{
		Document bpmn = DocumentHelper.parseText(xml);
		XMLQuery query=new XMLQuery(bpmn);
		String processQuery = "//bpmn:process";	
		Node process=query.selectSingleNode(processQuery);
		
		List<Node> tasks=query.selectNodes("bpmn:serviceTask",process);
		for(Node task:tasks){
			String delegate=((Element)task).attributeValue("delegateExpression");

			if(delegate==null)
				((Element)task).addAttribute("camunda:delegateExpression","${serviceClass}");	
		}

		((Element)process).addAttribute("isExecutable","true");
		
		return bpmn.asXML();
	}
	
	private String createFile(String name, String xml) throws FileNotFoundException, UnsupportedEncodingException{
		 String fileName="processes/"+name+".bpmn";
		 File fichero=new File(fileName);
		 PrintWriter writer = new PrintWriter(fichero, "UTF-8");
		 writer.print(xml);
		 writer.close();
		 return fileName;
	}
	
	private void deployBPMNProcess() throws IOException{
		 final Resource[] resources = this.resourceLoader.getResources("file:" + System.getProperty("user.dir") + "/processes/*.bpmn");
	     System.out.println("Loaded Processes: "+resources.length);
	     config.setDeploymentResources(resources);
	     config.buildProcessEngine();
	}
	
	@RequestMapping(
			  value = "/process/{name}", 
			  method = RequestMethod.GET,
			  produces = "application/json")
	 @Transactional
	 public String getFragmentBPMN(@PathVariable(value="name") String name) throws IOException {
		 final Resource[] resources = this.resourceLoader.getResources("file:" + System.getProperty("user.dir") + "/processes/"+name+".bpmn");
		 if(resources.length==1){
			 return new String(Files.readAllBytes(Paths.get(resources[0].getURI())));
		 }
		 else return "";
	 }
	
	
	 @RequestMapping(
			  value = "/processes", 
			  method = RequestMethod.GET,
			  produces = "application/json")
	 @Transactional
	 public List<Map<String, Object>> getProcesses() {
		 return dao.getProcesses();
	 }
	 
	 
	 @RequestMapping(
			  value = "/{process}/start", 
			  method = RequestMethod.GET)
	 public String startProcessByID(@PathVariable(value="process") String processID) {
		 runtimeService.startProcessInstanceByKey(processID);
		 return "Process "+processID+" started";
	 }
	 
	 @RequestMapping(
			  value = "process/start", 
			  method = RequestMethod.GET)
	 public String startDefaultProcess() {
		 YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
         yamlFactory.setResources(new ClassPathResource("application.yml"));
         Properties props=yamlFactory.getObject();
         
         String processID=props.getProperty("decoder.defaultProcessID");
         
		 runtimeService.startProcessInstanceByKey(processID);
		 return "Process "+processID+" started";
	 }

}
