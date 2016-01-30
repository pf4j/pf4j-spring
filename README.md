PF4J - Spring Framework integration
=====================
[![Travis CI Build Status](https://travis-ci.org/decebals/pf4j-spring.png)](https://travis-ci.org/decebals/pf4j-spring)
<!--
[![Coverage Status](https://coveralls.io/repos/decebals/pf4j-spring/badge.svg?branch=master&service=github)](https://coveralls.io/github/decebals/pf4j-spring?branch=master)
[![Maven Central](http://img.shields.io/maven-central/v/ro.fortsoft.pf4j/pf4j-spring.svg)](http://search.maven.org/#search|ga|1|pf4j-spring)
-->

This project is a proof of concept related to how you can integrate [PF4J](https://github.com/decebals/pf4j) with Spring Framework.

Components
-------------------
- **ExtensionsInjector** allows PF4J's extensions to be expose as Spring beans.
- **SpringPlugin** your plugin extends this class if your plugin contains Spring beans
- **SpringExtensionFactory** use this ExtensionFactory in your PluginManager if you have SpringPlugins

Using Maven
-------------------
In your pom.xml you must define the dependencies to PF4J-Spring artifact with:

```xml
<dependency>
    <groupId>ro.fortsoft.pf4j</groupId>
    <artifactId>pf4j-spring</artifactId>
    <version>${pf4j-spring.version}</version>
</dependency>    
```

where ${pf4j-spring.version} is the last pf4j-spring version.

You may want to check for the latest released version using [Maven Search](http://search.maven.org/#search%7Cga%7C1%7Cpf4j-spring)

Also you can use the latest SNAPSHOT via the Sonatype Maven Repository. For this, you must add above lines in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>sonatype-nexus-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

How to use
-------------------
Create the Spring configuration (declare some beans) using annotations with:
```java
@Configuration
public class SpringConfiguration {

    @Bean
    public PluginManager pluginManager() {
        PluginManager pluginManager = new DefaultPluginManager() {

            @Override
            protected ExtensionFactory createExtensionFactory() {
                return new SpringExtensionFactory(this);
            }

        };
        pluginManager.loadPlugins();

        // start (active/resolved) the plugins
        pluginManager.startPlugins();

        return pluginManager;
    }

    @Bean
    public static ExtensionsInjector extensionsInjector() {
        return new ExtensionsInjector();
    }

    @Bean
    public Greetings greetings() {
        return new Greetings();
    }

}
```

Start your application (plain java code):
```java
public class Boot {

    public static void main(String[] args) {
        // retrieves the Spring application context
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        // retrieves automatically the extensions for the Greeting.class extension point
        Greetings greetings = applicationContext.getBean(Greetings.class);
        greetings.printGreetings();

        // stop plugins
        PluginManager pluginManager = applicationContext.getBean(PluginManager.class);
        /*
        // retrieves manually the extensions for the Greeting.class extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        System.out.println("greetings.size() = " + greetings.size());
        */
        pluginManager.stopPlugins();
    }

}
```

Consume the PF4J extensions as Spring beans:
```java
public class Greetings {

    @Autowired
    private List<Greeting> greetings;

    public void printGreetings() {
        System.out.println(String.format("Found %d extensions for extension point '%s'", greetings.size(), Greeting.class.getName()));
        for (Greeting greeting : greetings) {
            System.out.println(">>> " + greeting.getGreeting());
        }
    }

}
```

The output is:
```
Found 2 extensions for extension point 'ro.fortsoft.pf4j.demo.api.Greeting'
>>> Welcome
>>> Hello
```

Bellow I present you a more complex example where a plugin (see demo plugin2 - HelloPlugin) uses Spring Framework internally.

First, create an interface `MessageProvider` with an implementation class `HelloMessageProvider`
```java
public interface MessageProvider {

    public String getMessage();

}

public class HelloMessageProvider implements MessageProvider {

    @Override
    public String getMessage() {
        return "Hello";
    }

}
```

Declare the plugin's beans via Spring Configuration
```java
@Configuration
public class SpringConfiguration {

    @Bean
    public MessageProvider messageProvider() {
        return new HelloMessageProvider();
    }

}
```

Create my (Spring) plugin
```java
public class HelloPlugin extends SpringPlugin {

    public HelloPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("HelloPlugin.start()");
    }

    @Override
    public void stop() {
        System.out.println("HelloPlugin.stop()");
        super.stop(); // to close applicationContext
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setClassLoader(getWrapper().getPluginClassLoader());
        applicationContext.register(SpringConfiguration.class);
        applicationContext.refresh();

        return applicationContext;
    }

    @Extension
    public static class HelloGreeting implements Greeting {

        @Autowired
        private MessageProvider messageProvider;

        @Override
        public String getGreeting() {
//            return "Hello";
            // complicate a little bit the code
           return messageProvider.getMessage();
        }

    }

}
```

Ready, your extension is available in your application via `PluginManager` or `Spring Autowire`.

For more details please see the demo application.

Implementation details
-------------------
__ExtensionsInjector__ injects each PF4J's extension as a bean in Spring Framework. For example if you run the demo application
you will see these lines in log:

```
2014-06-16 16:40:36,573 DEBUG ro.fortsoft.pf4j.spring.ExtensionsInjector - Registering extensions of the plugin 'welcome-plugin' as beans
2014-06-16 16:40:36,586 DEBUG ro.fortsoft.pf4j.spring.ExtensionsInjector - Register extension 'ro.fortsoft.pf4j.demo.welcome.WelcomePlugin$WelcomeGreeting' as bean
2014-06-16 16:40:36,589 DEBUG ro.fortsoft.pf4j.spring.ExtensionsInjector - Registering extensions of the plugin 'hello-plugin' as beans
2014-06-16 16:40:36,589 DEBUG ro.fortsoft.pf4j.spring.ExtensionsInjector - Register extension 'ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting' as bean
```

The bean name is the extension class name (for example 'ro.fortsoft.pf4j.demo.welcome.WelcomePlugin$WelcomeGreeting').

For more information please see the demo sources.

Demo
-------------------
I have a tiny demo application. The demo application is in demo package.

Run the pf4j-spring demo (Boot class contains the main method) from IDE (IntelliJ in my case) with these arguments as VM options:
```
-Dpf4j.mode=development
```

and working directory:
```
pf4j-spring/demo/app
```

License
--------------
Copyright 2014 Decebal Suiu

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
