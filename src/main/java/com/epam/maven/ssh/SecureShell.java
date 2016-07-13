package com.epam.maven.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Vitaliy Boyarsky
 */
public class SecureShell {

    @Getter
    @Setter
    private InputStream inputStream;

    @Getter
    @Setter
    private OutputStream outputStream;

    public void execute(Session session) throws JSchException {
        Channel channel = session.openChannel("shell");
        channel.setInputStream(inputStream);
        channel.setOutputStream(outputStream);
        channel.connect();
        while (!channel.isClosed()) ;
        channel.disconnect();
    }

}
