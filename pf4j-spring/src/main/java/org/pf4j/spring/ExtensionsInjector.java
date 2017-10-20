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
package org.pf4j.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
//@Component
public class ExtensionsInjector {

    private static final Logger log = LoggerFactory.getLogger(ExtensionsInjector.class);

    private final PluginManager pluginManager;

    public ExtensionsInjector(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @PostConstruct
    public void injectExtensions(SingletonBeanRegistry beanRegistry) {
        ExtensionFactory extensionFactory = pluginManager.getExtensionFactory();

        // add extensions from classpath (non plugin)
        Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
        for (String extensionClassName : extensionClassNames) {
            try {
                log.debug("Register extension '{}' as bean", extensionClassName);
                Class<?> extensionClass = getClass().getClassLoader().loadClass(extensionClassName);
                registerExtension(extensionFactory.create(extensionClass), beanRegistry);
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }

        // add extensions for each started plugin
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        for (PluginWrapper plugin : startedPlugins) {
            log.debug("Registering extensions of the plugin '{}' as beans", plugin.getPluginId());
            extensionClassNames = pluginManager.getExtensionClassNames(plugin.getPluginId());
            for (String extensionClassName : extensionClassNames) {
                try {
                    log.debug("Register extension '{}' as bean", extensionClassName);
                    Class<?> extensionClass = plugin.getPluginClassLoader().loadClass(extensionClassName);
                    registerExtension(extensionFactory.create(extensionClass), beanRegistry);
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    protected void registerExtension(Object extension, SingletonBeanRegistry beanRegistry) {
        beanRegistry.registerSingleton(extension.getClass().getName(), extension);
    }

}
