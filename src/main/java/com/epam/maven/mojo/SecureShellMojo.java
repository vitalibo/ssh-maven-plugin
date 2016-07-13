package com.epam.maven.mojo;

import com.epam.maven.utils.Configuration;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vitaliy Boyarsky
 */
public abstract class SecureShellMojo extends AbstractMojo {

    @Component
    private Settings settings;

    @Parameter(alias = "ssh.server", property = "ssh.server", required = true)
    private String serverId;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final Server server = settings.getServer(serverId);
            if (server == null)
                throw new MojoFailureException("Not found server with id '" + serverId + "'. Please check %M2_HOME%/conf/settings.xml");
            final Configuration conf = new Configuration(server);
            final Session session = createSession(conf);
            session.connect();
            getLog().info("");
            getLog().info("Session connect to " + conf.get(Configuration.SSH_HOST) + ":" + conf.get(Configuration.SSH_PORT));
            execute(session, conf);
            session.disconnect();
            getLog().info("Session disconnect");
            getLog().info("");
        } catch (JSchException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public abstract void execute(Session session, Configuration conf)
            throws MojoExecutionException, MojoFailureException, JSchException;

    private Session createSession(Configuration conf) throws MojoExecutionException, JSchException {
        final JSch jsch = new JSch();
        Session session = jsch.getSession(conf.get(Configuration.SSH_USERNAME),
                conf.get(Configuration.SSH_HOST), Integer.parseInt(conf.get(Configuration.SSH_PORT)));
        if (conf.get(Configuration.SSH_PASSWORD) != null)
            session.setPassword(conf.get(Configuration.SSH_PASSWORD));
        session.setUserInfo(new SSHUserInfo(getLog()));
        return session;
    }

    private static class SSHUserInfo implements UserInfo {

        private static final Pattern pattern = Pattern.compile("^The authenticity of host .* can't be established");
        private final Scanner sc = new Scanner(System.in);
        private Log log;

        SSHUserInfo(Log log) {
            this.log = log;
        }

        @Override
        public String getPassphrase() {
            showMessage("Please enter your passphrase");
            return sc.nextLine();
        }

        @Override
        public String getPassword() {
            showMessage("Please enter your password");
            return sc.nextLine();
        }

        @Override
        public boolean promptPassword(String msg) {
            showMessage(msg);
            return promptYesNo();
        }

        @Override
        public boolean promptPassphrase(String msg) {
            showMessage(msg);
            return promptYesNo();
        }

        @Override
        public boolean promptYesNo(String message) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) return true;
            String[] args = message.split("\n");
            for (String msg : args) {
                log.warn(msg);
            }
            return promptYesNo();
        }

        @Override
        public void showMessage(String s) {
            log.info(s);
        }

        private boolean promptYesNo() {
            int rep = 3;
            do {
                showMessage("Please enter Yes or No [Y/N]");
                String line = sc.next().toUpperCase();
                if (line.equals("Y") || line.equals("YES")) {
                    return true;
                }
                if (line.equals("N") || line.equals("NO")) {
                    return false;
                }
            } while (rep-- > 0);
            throw new RuntimeException("Connection refused");
        }

    }

}