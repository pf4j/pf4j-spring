/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.fortsoft.pf4j.demo;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ro.fortsoft.pf4j.PluginManager;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class Boot {

    public static void main(String[] args) {
        // print logo
        printLogo();

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
