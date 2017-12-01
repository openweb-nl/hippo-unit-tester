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

package nl.openweb.hippo;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.openweb.hippo.exception.SetupTeardownException;


import static org.hippoecm.repository.api.HippoNodeType.HIPPO_PATHS;
import static org.junit.Assert.*;

/**
 * @author Ebrahim Aharpour
 * @since 9/12/2017
 */
public class BaseHippoTestTest extends BaseHippoTest {


    private static final String COMPONENT_NAME = "myComponent";

    @Test
    public void testHstRequest() {
        request.addParameter("q", "value01");
        assertEquals("value01", request.getParameter("q"));
        assertNull(request.getParameter("test"));

        assertNotNull(request.getParameterMap());
        assertArrayEquals(new String[]{"value01"}, (String[]) request.getParameterMap().get("q"));

        assertEquals(requestContext, request.getRequestContext());
        request.setAttribute("test", "value");
        assertEquals("value", request.getAttribute("test"));

        assertNotNull(request.getAttributeMap());
        request.getAttributeMap().put("test", "value");
        assertEquals("value", request.getAttributeMap().get("test"));

    }

    @Test
    public void setComponentTest() {
        componentManager.addComponent(COMPONENT_NAME, 1);
        assertEquals((Object) 1, componentManager.getComponent(COMPONENT_NAME));
    }

    @Test
    public void nullComponentTest() {
        assertNull(componentManager.getComponent(COMPONENT_NAME));
    }

    @Test
    public void recalculateHippoPathsTest() throws RepositoryException {
        recalculateHippoPaths();
        Node node = rootNode.getNode("content/documents");
        testHippoPaths(node);
    }

    @Test
    public void recalculateHippoPathsWithPathTest() throws RepositoryException {
        recalculateHippoPaths("/content/documents/mychannel");
        Node node = rootNode.getNode("content/documents/mychannel");
        testHippoPaths(node);
        node = rootNode.getNode("content/documents");
        assertFalse(node.hasProperty(HIPPO_PATHS));
    }

    private void testHippoPaths(Node node) throws RepositoryException {
        Property property = node.getProperty(HIPPO_PATHS);
        for (Value value : property.getValues()) {
            assertEquals(node.getIdentifier(), value.getString());
            if (!node.isSame(rootNode)) {
                node = node.getParent();
            }
        }
    }

    @Before
    public void setup() {
        try {
            super.setup();
            registerNodeType("ns:NewsPage", "ns:AnotherType");
            importer.createNodesFromXml(getResourceAsStream("/nl/openweb/hippo/demo/news.xml"),
                    "/content/documents/mychannel/news", "hippostd:folder");
        } catch (RepositoryException e) {
            throw new SetupTeardownException(e);
        }
    }

    @After
    public void teardown() {
        super.teardown();
    }

    @Override
    protected String getAnnotatedClassesResourcePath() {
        return "classpath*:org/onehippo/forge/**/*.class, " +
                "classpath*:com/onehippo/**/*.class, " +
                "classpath*:org/onehippo/cms7/hst/beans/**/*.class, " +
                "classpath*:nl/openweb/hippo/demo/domain/**/*.class";
    }

}