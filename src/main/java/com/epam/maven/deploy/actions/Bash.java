package com.epam.maven.deploy.actions;

import com.epam.maven.deploy.Action;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Vitaliy Boyarsky
 */
public class Bash implements Action {

    @Override
    public void execute(Session session, Map<Pattern, String> properties, Log logger)
            throws IOException, JSchException {

    }

}
