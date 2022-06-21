# Remove maven package from AWS codeartifact

This repository is a maven plugin to allows delete maven package from AWS codeartifact before deploy the maven package version.

## Getting Started

To install this plugin locally, run the following commands:

<br>

```bash
git clone https://github.com/Fred78290/delete-codeartifact-maven-plugin.git
cd delete-codeartifact-maven-plugin
mvn clean install
```

The documentation plugin is available [here](https://fred78290.github.io/delete-codeartifact-maven-plugin/)

## Usage in POM

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.aldunelabs.maven.codeartifact</groupId>
                <artifactId>delete-codeartifact-maven-plugin</artifactId>
                <version>1.0.0</version>
                <configuration>
                    <region>${env.CODEARTIFACT_REGION}</region>
                    <owner>${env.CODEARTIFACT_DOMAIN_OWNER}</owner>
                    <domain>${env.CODEARTIFACT_DOMAIN}</domain>
                    <repository>${env.CODEARTIFACT_REPOSITORY}</repository>
                    <profile>${env.CODEARTIFACT_PROFILE}</profile>
                    <accesskey>${env.CODEARTIFACT_ACCESSKEY}</accesskey>
                    <secretkey>${env.CODEARTIFACT_SECRETKEY}</secretkey>
                    <token>${env.CODEARTIFACT_SESSIONTOKEN}</token>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

## Plugin repository

```xml
    <pluginRepositories>
        <pluginRepository>
            <id>com.aldunelabs.maven.codeartifact</id>
            <url>https://maven.pkg.github.com/Fred78290/delete-codeartifact-maven-plugin</url>
            </pluginRepository>
    </pluginRepositories>
```

## CHANGELOG

- 1.0.2: Add delay before return: try to avoid 409 conflict occuring just after delete package operation