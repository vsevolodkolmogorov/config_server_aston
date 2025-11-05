package ru.astondevs.configserveraston.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ConfigSourceService {
    private final Path repo;

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public ConfigSourceService(@Value("${config.repository}") String repoPath) {
        this.repo = Paths.get(repoPath);

        System.out.println("Config repo path: " + repo.toAbsolutePath());
        System.out.println("Files in repo: " + Arrays.toString(repo.toFile().listFiles()));
    }

    public Map<String,Object> loadAsPropertySources(String app, String profile, String label) {
        String[] profiles = profile.split(",");
        List<String> profilesList = Arrays.asList(profiles);

        List<Map<String,Object>> propertySources = new ArrayList<>();

        List<Path> candidates = new ArrayList<>();

        for(String p: profilesList) {
            candidates.add(repo.resolve("application-" + p + ".yml"));
        }
        candidates.add(repo.resolve("application.yml"));

        for(String p: profilesList) {
            candidates.add(repo.resolve(app + "-" + p + ".yml"));
        }
        candidates.add(repo.resolve(app + ".yml"));

        for(Path path : candidates) {
            if (Files.exists(path)) {
                try {
                    Map<String, Object> map = yamlMapper.readValue(Files.newInputStream(path),
                            new TypeReference<Map<String,Object>>() {});
                    Map<String,Object> ps = new HashMap<>();
                    ps.put("name", "file:" + path.getFileName().toString());
                    ps.put("source", flatten(map));
                    propertySources.add(ps);
                } catch (IOException e) {
                    // log and continue
                }
            }
        }

        Map<String,Object> result = new HashMap<>();
        result.put("name", app);
        result.put("profiles", profilesList);
        result.put("label", label);
        result.put("version", String.valueOf(System.currentTimeMillis()));
        result.put("propertySources", propertySources);
        return result;
    }

    private Map<String,Object> flatten(Map<String,Object> map) {
        Map<String,Object> result = new LinkedHashMap<>();
        flattenRec("", map, result);
        return result;
    }

    private void flattenRec(String prefix, Map<String,Object> map, Map<String,Object> out) {
        for(Map.Entry<String,Object> e : map.entrySet()) {
            String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
            Object val = e.getValue();
            if (val instanceof Map) {
                flattenRec(key, (Map<String,Object>) val, out);
            } else {
                out.put(key, val);
            }
        }
    }
}
