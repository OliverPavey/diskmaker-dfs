# Development Notes

#### Project creation

Created Project using:

```
mvn archetype:generate \
  -DinteractiveMode=false \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DgroupId=diskmaker \
  -DartifactId=diskmaker-dfs 
```

Then added properties, and build resources to pom.xml:

```
<properties>
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>
</properties>
```

```
<build>
  <resources>
      <resource>
          <directory>src/main/resources</directory>
          <filtering>false</filtering>
      </resource>
  </resources>
  <testResources>
      <testResource>
          <directory>src/test/resources</directory>
          <filtering>false</filtering>
      </testResource>
  </testResources>
</build>
```

And then into the bottom of <build> added this so the created with 'mvn package' will be executable:

```
<plugins>
  <plugin>
  	<groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.2</version>
    <executions>
      <execution>
        <goals>
          <goal>jar</goal>
        </goals>
        <id>jar</id>
      </execution>
    </executions>
    <configuration>
      <archive>
        <manifest>
          <addClasspath>true</addClasspath>
          <mainClass>diskmaker.DiskMaker</mainClass>
        </manifest>
      </archive>
    </configuration>
  </plugin>
</plugins>
```

Also changed the version of JUnit to use to version 4.12.

```
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>4.12</version>
  <scope>test</scope>
</dependency>
```

Right clicked the project in package explorer, and choose "Maven", "Update Project...".

N.B. The "test runner" may need to be updated in Run Configurations when moving from JUnit 3 to 4.

#### Documentation

The DFS format is defined on pages 102 and 103 of the 
[BBC DFS User Guide](http://chrisacorns.computinghistory.org.uk/docs/Acorn/Manuals/Acorn_DiscSystemUGI2.pdf).

#### Viewing output

To view the bytes of an image file from Linux (including from a docker machine) use:

```
od -A x -t x1u1c test.img
```