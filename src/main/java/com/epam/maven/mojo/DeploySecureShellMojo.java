package com.epam.maven.mojo;

import com.epam.maven.deploy.Deployfile;
import com.epam.maven.deploy.Script;
import com.epam.maven.util.Configuration;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * @author Vitaliy Boyarsky
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DeploySecureShellMojo extends SecureShellMojo {

    @Parameter(alias = "ssh.deployfile.path", property = "ssh.deployfile.path", defaultValue = "Deploy.json")
    private File deployfile;

    @Parameter(alias = "ssh.deploy.script", property = "ssh.deploy.script", required = true)
    private String scriptName;

    @Component
    private MavenProject mavenProject;

    @Override
    public void execute(Session session, Configuration conf)
            throws MojoExecutionException, MojoFailureException, JSchException {
        try {
            Deployfile deployfile = Deployfile.readDeployfile(this.deployfile);
            Script script = deployfile.scriptOf(scriptName);
            conf.put("project.build.package", mavenProject.getBuild().getFinalName() + '.' + mavenProject.getPackaging());
            script.execute(session, conf, getLog());
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

}
