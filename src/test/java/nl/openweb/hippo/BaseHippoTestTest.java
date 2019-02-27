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

import nl.openweb.hippo.demo.domain.NewsPage;
import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.jcr.importer.XmlImporter;
import org.apache.commons.io.IOUtils;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hippoecm.repository.api.HippoNodeType.HIPPO_PATHS;
import static org.junit.Assert.*;

/**
 * @author Ebrahim Aharpour
 * @since 9/12/2017
 */
public class BaseHippoTestTest extends BaseHippoTest {


    private static final String COMPONENT_NAME = "myComponent";


    @Test
    public void testGetFileFormatByPathIsIgnoringCase() {
        Assert.assertEquals("bar", getFileFormatByPath("foo.BAR"));
        Assert.assertEquals("json", getFileFormatByPath("foo.Json"));
        Assert.assertEquals("json", getFileFormatByPath("foo.JSON"));
        Assert.assertEquals("json", getFileFormatByPath("foo.json"));
        Assert.assertEquals("xml", getFileFormatByPath("foo.xml"));
        Assert.assertEquals("xml", getFileFormatByPath("foo.XML"));
    }
    @Test
    public void testGetFileFormatByPathHandlesFileWithoutExtension() {
        Assert.assertNull(getFileFormatByPath(null));
        Assert.assertNull(getFileFormatByPath("foo"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetImporterShouldThrowExceptionForUnknownFormat() {
        getImporter("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetImporterShouldThrowExceptionForNullFormat() {
        getImporter(null);
    }

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
        recalculateHippoPaths("/content/documents/mychannel", true);
        Node node = rootNode.getNode("content/documents/mychannel");
        testHippoPaths(node);
        node = rootNode.getNode("content/documents");
        assertFalse(node.hasProperty(HIPPO_PATHS));
    }


    @Test
    public void recalculateHippoPathsNotSaveTest() throws RepositoryException, QueryException {
        recalculateHippoPaths(false);
        Node scope = rootNode.getNode("content/documents/mychannel");
        HstQuery query = HstQueryBuilder.create(scope)
                .ofTypes(NewsPage.class)
                .build();

        assertEquals(0, query.execute().getSize());
        rootNode.getSession().save();
        assertEquals(3, query.execute().getSize());
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
            getImporter(XmlImporter.FORMAT).createNodes(getResourceAsStream("/nl/openweb/hippo/demo/news.xml"),
                    "/content/documents/mychannel/news", "hippostd:folder");
        } catch (RepositoryException e) {
            throw new SetupTeardownException(e);
        }
    }

    @Test
    public void printNodeStructureTest() throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            printNodeStructure("/content", new PrintStream(os));

            String expectedValue = IOUtils.toString(
                    getResourceAsStream("expected-node-structure.txt")
                    , UTF_8);

            String actualValue = new String(os.toByteArray(), UTF_8);
            String osIndependentValue = actualValue.replace("\r\n", "\n").replace('\r', '\n');
            String osIndependentExpectedValue = expectedValue.replace("\r\n", "\n").replace('\r', '\n');
            assertEquals(osIndependentExpectedValue, osIndependentValue);
        }
    }

    @Test
    public void hippoMirrorTest() {
        NewsPage hippoBean = (NewsPage) getHippoBean("/content/documents/mychannel/news/news3");
        assertEquals("news2", hippoBean.getRelatedNews().getName());
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