/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
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
package nl.openweb.hippo.mock;

import org.hippoecm.hst.core.container.ComponentsException;
import org.hippoecm.hst.mock.core.container.MockContainerConfiguration;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Ivor
 */
public class MockComponentManagerTest {
    private final MockComponentManager componentManager = new MockComponentManager();


    @Test
    public void getContainerConfigurationTest() {
        MockContainerConfiguration containerConfiguration = componentManager.getContainerConfiguration();
        Assert.assertTrue(containerConfiguration instanceof MockContainerConfiguration);
    }

    @Test
    public void setServletConfigTestWithNull() {
        ServletContext context = mock(ServletContext.class);
        componentManager.setServletContext(context);
        Assert.assertEquals(context, componentManager.getServletContext());
    }

    @Test
    public void setServletConfig() {
        ServletConfig servletConfig = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servletConfig).getServletContext();

        componentManager.setServletConfig(servletConfig);

        Assert.assertEquals(servletConfig, componentManager.getServletConfig());
        Assert.assertEquals(context, componentManager.getServletContext());
    }

    @Test
    public void getComponentsOfTypeTest() {
        String component1 = "component1";
        String component2 = "component2";
        componentManager.addComponent("test1", component1);
        componentManager.addComponent("test2", component2);

        Map<String, String> componentsOfType = componentManager.getComponentsOfType(String.class);
        Assert.assertEquals(2, componentsOfType.size());


    }

    @Test
    public void getComponentTest() {
        String component1 = "component1";
        String component2 = "component2";
        componentManager.addComponent("test1", component1);
        componentManager.addComponent("test2", component2);

        String result1 = componentManager.getComponent("test1");
        String result2 = componentManager.getComponent("component1");
        Assert.assertEquals(component1, result1);
        Assert.assertNull(result2);
    }

    @Test
    public void getComponentTestWithModuleName() {
        String component1 = "component1";
        String component2 = "component2";
        componentManager.addComponent("test1", component1);
        componentManager.addComponent("test2", component2);

        String result1 = componentManager.getComponent("test1", "foo");
        String result2 = componentManager.getComponent("component1", "foo");
        Assert.assertEquals(component1, result1);
        Assert.assertNull(result2);
    }

    @Test
    public void getComponentByClassTest() {
        String component1 = "component1";
        componentManager.addComponent("test1", component1);

        String result1 = componentManager.getComponent(String.class);
        Assert.assertEquals(component1, result1);
        Double result2 = componentManager.getComponent(Double.class);
        Assert.assertNull(result2);
    }

    @Test(expected = ComponentsException.class)
    public void getComponentByClassTestWithMultipleComponentsOfSameType() {
        String component1 = "component1";
        Integer component2 = 2;
        Integer component3 = 3;
        componentManager.addComponent("test1", component1);
        componentManager.addComponent("test2", component2);
        componentManager.addComponent("test3", component3);

        componentManager.getComponent(Integer.class);
        Assert.fail();
    }


    @Test
    public void getComponentByClassTestWithAddonModuleName() {
        String component1 = "component1";
        componentManager.addComponent("test1", component1);

        String result1 = componentManager.getComponent(String.class, "foo");
        Assert.assertEquals(component1, result1);
        Double result2 = componentManager.getComponent(Double.class, "foo");
        Assert.assertNull(result2);
    }
}
