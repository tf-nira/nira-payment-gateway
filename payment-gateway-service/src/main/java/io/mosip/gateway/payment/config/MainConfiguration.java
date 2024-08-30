package io.mosip.gateway.payment.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class MainConfiguration {
	
	@Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Payment Gateway Service API")
                .description("Payment Gateway Service application")
                .version("v0.0.1"));
    }
	
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("io.mosip.gateway.payment");
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("jaxb.formatted.output", false);
		jaxb2Marshaller.setMarshallerProperties(props);
		return jaxb2Marshaller;
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	


}
