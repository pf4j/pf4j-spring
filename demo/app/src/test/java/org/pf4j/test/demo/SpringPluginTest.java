package org.pf4j.test.demo;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class SpringPluginTest {
    @Autowired
    private SpringPluginManager pluginManager;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("pf4j.mode", "development");
    }

    @Test
    public void testStartStopStartRuns() {
        final String PLUGIN_ID = "hello-plugin";

        pluginManager.startPlugin(PLUGIN_ID);
        pluginManager.getExtensions(PLUGIN_ID);
        pluginManager.stopPlugin(PLUGIN_ID);

        pluginManager.startPlugin(PLUGIN_ID);
        pluginManager.getExtensions(PLUGIN_ID);
        pluginManager.stopPlugin(PLUGIN_ID);
    }
}
