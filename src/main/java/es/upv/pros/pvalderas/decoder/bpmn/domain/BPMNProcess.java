package es.upv.pros.pvalderas.decoder.bpmn.domain;

public class BPMNProcess
{
    private String name;
    private String id;
    private String xml;
    private String microservice;
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public String getXml() {
        return this.xml;
    }
    
    public void setXml(final String xml) {
        this.xml = xml;
    }
    
    public String getMicroservice() {
        return this.microservice;
    }
    
    public void setMicroservice(final String microservice) {
        this.microservice = microservice;
    }
}