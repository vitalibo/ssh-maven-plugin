# SSH Maven Plugin Documentation

The plugin provides 2 goals to help deploy and execute system and Java programs on remote servers.

## Goals available

Goal                | Description
--------------------|----------------------------------------
`ssh:deploy`        | A goal for execution script on remote server
`ssh:template`      | Create template Deploy file

## Usage

In order to deploy project using SSH you must first specify the use of an SSH server. For this you need add configuration
in your `settings.xml`

```
<servers>
    ...
    <server>
        <id>hdp2.4</id>
        <username>root</username>
        <password>hadoop</password>
        <privateKey>${user.home}/.ssh/id_rsa</privateKey>
        <configuration>
            <host>localhost</host>
            <port>2222</port>
        </configuration>
    </server>
    ...
</servers>
```

Now you should specify the artifact version in your project's plugin configuration

```
<build>
    <plugins>
        ...
        <plugin>
            <groupId>com.epam.maven</groupId>
            <artifactId>ssh-maven-plugin</artifactId>
            <version>1.7.3</version>
            <configuration>
                <ssh.server>hdp2.4</ssh.server>
            </configuration>
        </plugin>
        ...
    </plugins>
</build>
```

When you use the `deploy` goal, you might want to change the deploy file of the plugin execution. This can be achieved 
using the `ssh.deployfile.path` configuration element. By default deploy file location in root folder of the project. 
For select your executable script you need use configuration element `ssh.deploy.script`, for example:

```
<configuration>
    ...
    <ssh.deployfile.path>deployment/Blueprint.json</ssh.deployfile.path>
    <ssh.deploy.script>spark-streaming</ssh.deploy.script>
    ...
</configuration>
```

or, on the command line:

```
mvn package ssh:deploy -Dssh.deploy.script=spark-streaming
```

## Configure deploy file

You can create template deploy file use the `template` goal, after execution will be created following file template:

```
{
  "version" : "1.7.3",
  "scripts" : [ {
    "name" : "demo",
    "properties" : {
      "jdk.version" : "1.7"
    },
    "actions" : [ {
      "type" : "scp",
      "items" : [ {
        "source" : "target/${project.build.package}",
        "target" : "."
      } ]
    }, {
      "type" : "bash",
      "items" : [ "java -cp ${project.build.package}" ]
    } ]
  } ]
}
```

Deploy file should contains _`version`_ and _`list of scripts`_. Now supported _`1.7.[1-3]`_ version of deploy file.
Script object should contains _`name`_ and _`list of actions`_, optional _`list of properties`_, _`description`_ and _`author`_. 
Any action should contains field _`type`_ and other fields typical for specific implementation. Now available following actions:

Action      | Description
------------|------------------------------------------------
`scp`       | Implementation [Secure Copy](https://en.wikipedia.org/wiki/Secure_copy). Contains list of object (_`source`_ and optional _`target`_)
`bash`      | Command line on remote server, each string of list will be executed remote

