package com.epam.maven.utils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vitaliy Boyarsky
 */
public class Configuration extends HashMap<String, String> {

    public static final String SSH_USERNAME = "ssh.username";
    public static final String SSH_PASSWORD = "ssh.password";
    public static final String SSH_HOST = "ssh.host";
    public static final String SSH_PORT = "ssh.port";

    public Configuration() {
        super();
    }

    public Configuration(Server server) throws MojoExecutionException, MojoFailureException {
        this.put(SSH_USERNAME, server.getUsername());
        this.put(SSH_PASSWORD, server.getPassword());
        InputStream input = new ByteArrayInputStream(server.getConfiguration().toString().getBytes(StandardCharsets.UTF_8));
        this.putAll(parseServerSettings(input));
    }

    private static String getElementsByTagName(Element conf, String name, String defaultValue) {
        NodeList elementsByTagName = conf.getElementsByTagName(name);
        if (!(elementsByTagName.getLength() > 0)) return defaultValue;
        return elementsByTagName.item(0).getTextContent().trim();
    }

    private static Map<String, String> parseServerSettings(InputStream inputStream) throws MojoExecutionException, MojoFailureException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        final Element conf = (Element) document.getElementsByTagName("configuration").item(0);
        return new HashMap<String, String>() {
            {
                this.put(SSH_HOST, getElementsByTagName(conf, "host", "localhost"));
                this.put(SSH_PORT, getElementsByTagName(conf, "port", "22"));
            }
        };
    }

}
