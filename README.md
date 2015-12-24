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
- **ExtensionsInjector** allows PF4J's extensions to be expose as spring beans.

How to use
-------------------

Create the Spring configuration (declare some beans) using annotations with:

```java
@Configuration
public class AppConfig {

    @Bean
    public PluginManager pluginManager() {
        PluginManager pluginManager = new DefaultPluginManager();
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
        // retrieves the spring application context
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        // print greetings in System.out
        Greetings greetings = applicationContext.getBean(Greetings.class);
        greetings.printGreetings();

        // stop plugins
        PluginManager pluginManager = applicationContext.getBean(PluginManager.class);
        pluginManager.stopPlugins();
    }

    private static void printLogo() {
        System.out.println(StringUtils.repeat("#", 40));
        System.out.println(StringUtils.center("PF4J-SPRING", 40));
        System.out.println(StringUtils.repeat("#", 40));
    }

}
```

Consume the PF4J extensions as spring beans:

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

    Found 2 extensions for extension point 'ro.fortsoft.pf4j.demo.api.Greeting'
    >>> Welcome
    >>> Hello

Implementation details
-------------------

__ExtensionsInjector__ injects each PF4J's extension as a bean in spring framework. For example if you run the demo application
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

First checkout [PF4J](https://github.com/decebals/pf4j).
Run the pf4j demo application use:

    ./run-demo.sh (for Linux/Unix)
    ./run-demo.bat (for Windows)

Put __<pf4j_home>/demo-dist/lib/pf4j-demo-api.jar__ file in the pf4j-spring demo classpath.
Run the pf4j-spring demo (Boot class contains the main method) from IDE (IntelliJ in my case) with these arguments as VM options:
```
-Dpf4j.mode=development -Dpf4j.pluginsDir=<pf4j_home>/demo/plugins
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
