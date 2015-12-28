/*
 * Copyright 2015 Decebal Suiu
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.ExtensionFactory;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.spring.ExtensionsInjector;
import ro.fortsoft.pf4j.spring.SpringExtensionFactory;

/**
 * @author Decebal Suiu
 */
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
