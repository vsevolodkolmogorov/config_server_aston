package ru.astondevs.configserveraston;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerAstonApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerAstonApplication.class, args);
    }

}
