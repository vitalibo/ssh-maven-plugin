package com.epam.maven.mojo;

import com.epam.maven.deploy.Deployfile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * @author Vitaliy Boyarsky
 */
@Mojo(name = "create-dummy", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class CreateDummyMojo extends AbstractMojo {

    @Parameter(alias = "ssh.deployfile.path", property = "ssh.deployfile.path", defaultValue = "Deploy.json")
    private File deployfile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Deployfile.saveDeployfile(deployfile);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

}
