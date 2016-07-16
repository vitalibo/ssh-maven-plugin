package com.epam.maven.deploy;

import com.epam.maven.util.Configuration;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Vitaliy Boyarsky
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Script {

    @Getter
    @Setter
    @JsonProperty("name")
    private String name;

    @Getter
    @Setter
    @JsonProperty("description")
    private String description;

    @Getter
    @Setter
    @JsonProperty("author")
    private String author;

    @Getter
    @Setter
    @JsonProperty("properties")
    private Map<String, String> properties;

    @Getter
    @Setter
    @JsonProperty("actions")
    private List<Action> actions;

    private static Map<Pattern, String> toPattern(Map<String, String> original) {
        Map<Pattern, String> pattern = new HashMap<>();
        for (Map.Entry<String, String> entry : original.entrySet()) {
            pattern.put(Pattern.compile("\\$\\{" + entry.getKey().replaceAll("\\.", "\\\\.") + "\\}"), entry.getValue());
        }
        return pattern;
    }

    public void execute(Session session, Configuration conf, Log logger) throws IOException, JSchException {
        logger.info("Execution deploy script: " + name);
        Map<Pattern, String> properties = new HashMap<>();
        properties.putAll(toPattern(this.properties));
        properties.putAll(toPattern(conf));
        for (Action action : actions) {
            action.execute(session, properties, logger);
        }
        logger.info("Deploy script successfully finished");
    }

}