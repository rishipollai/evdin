package com.lti.idss.service.taxpayer.everification.list.business;

import com.lti.idss.servicemesh.client.IdssServiceMeshClient;
import io.opentracing.Tracer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"com.lti.idss.*"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@GetMapping
	public String welcome() {
		return "eVerification din List Business Service is up and running";
	}

	@Bean
	public Tracer tracer() throws Exception {
		return IdssServiceMeshClient.initTracer("service-taxpayer-everification-din-business");
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
