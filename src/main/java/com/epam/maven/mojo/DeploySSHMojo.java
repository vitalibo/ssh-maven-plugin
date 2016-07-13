package com.epam.maven.mojo;

import com.epam.maven.utils.Configuration;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author Vitaliy Boyarsky
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DeploySSHMojo extends SecureShellMojo {

    @Override
    public void execute(Session session, Configuration conf)
            throws MojoExecutionException, MojoFailureException, JSchException {

    }

}
