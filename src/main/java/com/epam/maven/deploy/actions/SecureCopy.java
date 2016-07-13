package com.epam.maven.deploy.actions;

import com.epam.maven.deploy.Action;
import com.epam.maven.ssh.SecureCopyProtocol;
import com.epam.maven.utils.Separator;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.epam.maven.utils.Replacer.replaceAll;

/**
 * @author Vitaliy Boyarsky
 */
public class SecureCopy implements Action {

    @Getter
    @Setter
    @JsonProperty("items")
    private List<Bean> items;

    @Getter
    @JsonProperty("to")
    private To to = To.REMOTE;

    public void setTo(String to) {
        this.to = To.valueOf(to);
    }

    @Override
    public void execute(Session session, Map<Pattern, String> properties, Log logger)
            throws IOException, JSchException {
        logger.info("Start copying files");
        logger.info(Separator.TO_STRING);
        System.out.println();

        for (Bean item : items) {
            switch (to) {
                case REMOTE:
                    SecureCopyProtocol.copyToRemote(session, replaceAll(properties, item.getSource()), replaceAll(properties, item.getTarget()));
                    break;
                case LOCAL:
                    SecureCopyProtocol.copyToLocal(session, replaceAll(properties, item.getSource()), replaceAll(properties, item.getTarget()));
                    break;
            }
        }

        System.out.println();
        logger.info(Separator.TO_STRING);
        logger.info("Copying successfully completed");
    }

    private enum To {

        REMOTE, LOCAL

    }

    public static class Bean {

        @Getter
        @Setter
        @JsonProperty("source")
        private String source;

        @Getter
        @Setter
        @JsonProperty("target")
        private String target = ".";

        public Bean() {
            super();
        }

        public Bean(String source, String target) {
            this.source = source;
            this.target = target;
        }

    }

}
