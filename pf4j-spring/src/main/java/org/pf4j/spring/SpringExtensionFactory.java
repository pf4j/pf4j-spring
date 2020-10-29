/*
 * Copyright (C) 2012-present the original author or authors.
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

import org.pf4j.Extension;
import org.pf4j.ExtensionFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

/**
 * Basic implementation of an extension factory.
 * <p><p>
 * Uses Springs {@link AutowireCapableBeanFactory} to instantiate a given extension class. All kinds of
 * {@link Autowired} are supported (see example below). If no {@link ApplicationContext} is available (this is the case
 * if either the related plugin is not a {@link SpringPlugin} or the given plugin manager is not a
 * {@link SpringPluginManager}), standard Java reflection will be used to instantiate an extension.
 * <p><p>
 * Creates a new extension instance every time a request is done.
 * <p><p>
 * Example of supported autowire modes:
 * <pre>{@code
 *     @Extension
 *     public class Foo implements ExtensionPoint {
 *
 *         private final Bar bar;       // Constructor injection
 *         private Baz baz;             // Setter injection
 *         @Autowired
 *         private Qux qux;             // Field injection
 *
 *         @Autowired
 *         public Foo(final Bar bar) {
 *             this.bar = bar;
 *         }
 *
 *         @Autowired
 *         public void setBaz(final Baz baz) {
 *             this.baz = baz;
 *         }
 *     }
 * }</pre>
 *
 * @author Decebal Suiu
 * @author m-schroeer
 */
public class SpringExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(SpringExtensionFactory.class);
    public static final boolean AUTOWIRE_BY_DEFAULT = true;

    private static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

    /**
     * The plugin manager is used for retrieving a plugin from a given extension class
     * and as a fallback supplier of an application context.
     */
    protected final PluginManager pluginManager;
    /**
     * Indicates if springs autowiring possibilities should be used.
     */
    protected final boolean autowire;

    public SpringExtensionFactory(final PluginManager pluginManager) {
        this(pluginManager, AUTOWIRE_BY_DEFAULT);
    }

    public SpringExtensionFactory(final PluginManager pluginManager, final boolean autowire) {
        this.pluginManager = pluginManager;
        this.autowire = autowire;
        if (!autowire) {
            log.warn("Autowiring is disabled although the only reason for existence of this special factory is" +
                     " supporting spring and its application context.");
        }
    }

    /**
     * Creates an instance of the given {@code extensionClass}. If {@link #autowire} is set to {@code true} this method
     * will try to use springs autowiring possibilities.
     *
     * @param extensionClass The class annotated with {@code @}{@link Extension}.
     * @param <T>            The type for that an instance should be created.
     * @return an instance of the the requested {@code extensionClass}.
     * @see #getApplicationContextBy(Class)
     */
    @Override
    public <T> T create(final Class<T> extensionClass) {
        if (!this.autowire) {
            log.warn("Create instance of '" + nameOf(extensionClass) + "' without using springs possibilities as" +
                     " autowiring is disabled.");
            return createWithoutSpring(extensionClass);
        }

        return getApplicationContextBy(extensionClass)
            .map(applicationContext -> createWithSpring(extensionClass, applicationContext))
            .orElseGet(() -> createWithoutSpring(extensionClass));
    }

    /**
     * Creates an instance of the given {@code extensionClass} by using the {@link AutowireCapableBeanFactory} of the given
     * {@code applicationContext}. All kinds of autowiring are applied:
     * <ol>
     *     <li>Constructor injection</li>
     *     <li>Setter injection</li>
     *     <li>Field injection</li>
     * </ol>
     *
     * @param extensionClass     The class annotated with {@code @}{@link Extension}.
     * @param <T>                The type for that an instance should be created.
     * @param applicationContext The context to use for autowiring.
     * @return an autowired extension instance.
     */
    @SuppressWarnings("unchecked")
    protected <T> T createWithSpring(final Class<T> extensionClass, final ApplicationContext applicationContext) {
        final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

        log.debug("Instantiate extension class '" + nameOf(extensionClass) + "' by using constructor autowiring.");
        // Autowire by constructor. This does not include the other types of injection (setters and/or fields).
        final Object autowiredExtension = beanFactory.autowire(extensionClass, AUTOWIRE_CONSTRUCTOR,
            // The value of the 'dependencyCheck' parameter is actually irrelevant as the using constructor of 'RootBeanDefinition'
            // skips action when the autowire mode is set to 'AUTOWIRE_CONSTRUCTOR'. Although the default value in
            // 'AbstractBeanDefinition' is 'DEPENDENCY_CHECK_NONE', so it is set to false here as well.
            false);
        log.trace("Created extension instance by constructor injection: " + autowiredExtension);

        log.debug("Completing autowiring of extension: " + autowiredExtension);
        // Autowire by using remaining kinds of injection (e. g. setters and/or fields).
        beanFactory.autowireBean(autowiredExtension);
        log.trace("Autowiring has been completed for extension: " + autowiredExtension);

        return (T) autowiredExtension;
    }

    /**
     * Retrieves springs {@link ApplicationContext} from the extensions plugin or the {@link #pluginManager}.
     * <p>
     * The ordering of checks is:
     * <ol>
     *     <li>If the given {@code extensionClass} belongs to a plugin that is a {@link SpringPlugin} the plugins context will be returned.</li>
     *     <li>Otherwise, if the given {@link #pluginManager} of this instance is a {@link SpringPluginManager} the managers context will be returned.</li>
     *     <li>If none of these checks fits, {@code null} is returned.</li>
     * </ol>
     *
     * @param extensionClass The class annotated with {@code @}{@link Extension}.
     * @param <T>            The Type of extension for that an {@link ApplicationContext} is requested.
     * @return the best fitting context, or {@code null}.
     */
    protected <T> Optional<ApplicationContext> getApplicationContextBy(final Class<T> extensionClass) {
        final Plugin plugin = Optional.ofNullable(this.pluginManager.whichPlugin(extensionClass))
            .map(PluginWrapper::getPlugin)
            .orElse(null);

        final ApplicationContext applicationContext;

        if (plugin instanceof SpringPlugin) {
            log.debug("  Extension class ' " + nameOf(extensionClass) + "' belongs to spring-plugin '" + nameOf(plugin)
                      + "' and will be autowired by using its application context.");
            applicationContext = ((SpringPlugin) plugin).getApplicationContext();
        } else if (this.pluginManager instanceof SpringPluginManager) {
            log.debug("  Extension class ' " + nameOf(extensionClass) + "' belongs to a non spring-plugin (or main application)" +
                      " '" + nameOf(plugin) + ", but the used PF4J plugin-manager is a spring-plugin-manager. Therefore" +
                      " the extension class will be autowired by using the managers application contexts");
            applicationContext = ((SpringPluginManager) this.pluginManager).getApplicationContext();
        } else {
            log.warn("  No application contexts can be used for instantiating extension class '" + nameOf(extensionClass) + "'."
                     + " This extension neither belongs to a PF4J spring-plugin (id: '" + nameOf(plugin) + "') nor is the used" +
                     " plugin manager a spring-plugin-manager (used manager: '" + nameOf(this.pluginManager.getClass()) + "')." +
                     " At perspective of PF4J this seems highly uncommon in combination with a factory which only reason for existence" +
                     " is using spring (and its application context) and should at least be reviewed. In fact no autowiring can be" +
                     " applied although autowire flag was set to 'true'. Instantiating will fallback to standard Java reflection.");
            applicationContext = null;
        }

        return Optional.ofNullable(applicationContext);
    }

    /**
     * Creates an instance of the given class object by using standard Java reflection.
     *
     * @param extensionClass The class annotated with {@code @}{@link Extension}.
     * @param <T>            The type for that an instance should be created.
     * @return an instantiated extension.
     * @throws IllegalArgumentException if the given class object has no public constructor.
     * @throws RuntimeException         if the called constructor cannot be instantiated with {@code null}-parameters.
     */
    @SuppressWarnings("unchecked")
    protected <T> T createWithoutSpring(final Class<T> extensionClass) throws IllegalArgumentException {
        final Constructor<?> constructor = getPublicConstructorWithShortestParameterList(extensionClass)
            // An extension class is required to have at least one public constructor.
            .orElseThrow(() -> new IllegalArgumentException("Extension class '" + nameOf(extensionClass)
                                                            + "' must have at least one public constructor."));
        try {
            log.debug("Instantiate '" + nameOf(extensionClass) + "' by calling '" + constructor + "'with standard Java reflection.");
            // Creating the instance by calling the constructor with null-parameters (if there are any).
            return (T) constructor.newInstance(nullParameters(constructor));
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            // If one of these exceptions is thrown it it most likely because of NPE inside the called constructor and
            // not the reflective call itself as we precisely searched for a fitting constructor.
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("Most likely this exception is thrown because the called constructor (" + constructor + ")" +
                                       " cannot handle 'null' parameters. Original message was: "
                                       + ex.getMessage(), ex);
        }
    }

    private Optional<Constructor<?>> getPublicConstructorWithShortestParameterList(final Class<?> extensionClass) {
        return Stream.of(extensionClass.getConstructors())
            .min(Comparator.comparing(Constructor::getParameterCount));
    }

    private Object[] nullParameters(final Constructor<?> constructor) {
        return new Object[constructor.getParameterCount()];
    }

    private String nameOf(final Plugin plugin) {
        return nonNull(plugin)
            ? plugin.getWrapper().getPluginId()
            : "system";
    }

    private <T> String nameOf(final Class<T> clazz) {
        return clazz.getName();
    }
}
