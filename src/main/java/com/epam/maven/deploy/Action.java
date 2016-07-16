package com.epam.maven.deploy;

import com.epam.maven.deploy.action.Bash;
import com.epam.maven.deploy.action.SecureCopy;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Vitaliy Boyarsky
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SecureCopy.class, name = "scp"),
        @JsonSubTypes.Type(value = Bash.class, name = "bash"),
})
public interface Action {

    void execute(Session session, Map<Pattern, String> properties, Log logger) throws IOException, JSchException;

}
