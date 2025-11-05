package ru.astondevs.configserveraston.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ServiceBootstrapEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configUrl = environment.getProperty("config.server.url");
        if (configUrl == null) return;

        String appName = environment.getProperty("spring.application.name", "application");
        String profiles = environment.getProperty("spring.profiles.active", "default");

        String url = String.format("%s/config/%s/%s", configUrl, appName, profiles);

        try {
            Map body = rest.getForObject(url, Map.class);
            if (body == null) return;
            List<Map<String,Object>> propertySources = (List) body.get("propertySources");
            MutablePropertySources sources = environment.getPropertySources();

            for(int i = propertySources.size()-1; i>=0; i--) {
                Map ps = propertySources.get(i);
                Map<String,Object> src = (Map<String,Object>) ps.get("source");
                PropertySource<?> prop = new MapPropertySource("remote-config:" + ps.get("name"), src);
                sources.addFirst(prop);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}
