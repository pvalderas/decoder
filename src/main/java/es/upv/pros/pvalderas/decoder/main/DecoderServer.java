package es.upv.pros.pvalderas.decoder.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.upv.pros.pvalderas.decoder.server.DecoderProcessServer;

@DecoderProcessServer
@SpringBootApplication(scanBasePackages = {"es.upv.pros.pvalderas.decoder.server"})
public class DecoderServer {
	
	public static void main(String[] args) {
		SpringApplication.run(DecoderServer.class, args);
	}
	
		
}