package com.ticketti.ms_usuarios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Value("${server.port:8080}")
	private String serverPort;

	@Bean
	public OpenAPI myOpenAPI() {
		Server devServer = new Server();
		devServer.setUrl("http://localhost:" + serverPort);
		devServer.setDescription("Servidor de desarrollo");

		Contact contact = new Contact();
		contact.setEmail("ticketii@duoc.cl");
		contact.setName("NexGen Solutions");

		License mitLicense = new License()
				.name("MIT License")
				.url("https://choosealicense.com/licenses/mit/");

		Info info = new Info()
				.title("Usuario Management API")
				.version("1.0")
				.contact(contact)
				.description("API REST para gestion de usuarios con roles y permisos")
				.license(mitLicense);

		return new OpenAPI()
				.info(info)
				.servers(List.of(devServer));
	}
}
