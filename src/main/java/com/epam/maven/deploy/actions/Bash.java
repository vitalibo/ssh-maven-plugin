package com.epam.maven.deploy.actions;

import com.epam.maven.deploy.Action;
import com.epam.maven.ssh.SecureShell;
import com.epam.maven.utils.Separator;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.epam.maven.utils.Replacer.replaceAll;

/**
 * @author Vitaliy Boyarsky
 */
public class Bash implements Action {

    @Getter
    @Setter
    @JsonProperty("items")
    private List<String> items;

    // Join all commands in one line
    private static String joinToLine(List<String> bash) {
        StringBuilder sb = new StringBuilder((1 + bash.size()) * 2);
        for (String cmd : bash) {
            sb.append(cmd).append('\n');
        }
        sb.append("exit").append('\n'); // to exit with bash
        return sb.toString();
    }

    // Convert List of command into InputStream
    public static InputStream toInputStream(Map<Pattern, String> properties, List<String> items) {
        return new ByteArrayInputStream(replaceAll(properties, joinToLine(items)).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void execute(Session session, Map<Pattern, String> properties, Log logger)
            throws IOException, JSchException {
        logger.info("Running Bash scripts in deploy machine");
        logger.info(Separator.TO_STRING);
        System.out.println();

        SecureShell ssh = new SecureShell();
        // TODO Implementing other output stream (stdout, file, none)
        ssh.setOutputStream(new PrintStream(System.out) {
            @Override
            public void close() {
                // Ignored closing output stream
            }
        });
        // TODO Add cd  + deployDirectory
        ssh.setInputStream(toInputStream(properties, items));
        ssh.execute(session);

        System.out.println();
        logger.info(Separator.TO_STRING);
        logger.info("");
    }

}
