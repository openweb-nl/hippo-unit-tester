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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;

import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.linking.HstLink;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.openweb.hippo.BaseHippoTest;


import static org.junit.Assert.assertEquals;

/**
 * @author Ebrahim Aharpour
 * @since 11/29/2017
 */
public class MockHstLinkCreatorTest extends BaseHippoTest {

    private static final MockHstLink HST_LINK = new MockHstLink("https://www.openweb.nl", "/diensten");

    @Before
    public void setup() {
        super.setup();
        importer.createNodesFromXml(getResourceAsStream("/nl/openweb/hippo/test/news.xml"),
                "/content/documents/mychannel/news", "hippostd:folder");

    }

    @Test
    public void addHstLinkViaBean() throws RepositoryException {
        HippoBean hippoBean = getHippoBean("/content/documents/mychannel/news/news1");
        hstLinkCreator.addHstLink(hippoBean, HST_LINK);
        testLinkViaBeanAndNodeAndUuid(hippoBean);
    }

    @Test
    public void addHstLinkViaNode() throws RepositoryException {
        HippoBean hippoBean = getHippoBean("/content/documents/mychannel/news/news1");
        hstLinkCreator.addHstLink(hippoBean.getNode(), HST_LINK);
        testLinkViaBeanAndNodeAndUuid(hippoBean);
    }

    @Test
    public void addHstLinkViaHandleNode() throws RepositoryException {
        HippoBean hippoBean = getHippoBean("/content/documents/mychannel/news/news1");
        hstLinkCreator.addHstLink(hippoBean.getNode().getParent(), HST_LINK);
        testLinkViaBeanAndNodeAndUuid(hippoBean);
    }

    @Test
    public void addHstLinkViaUuid() throws RepositoryException {
        HippoBean hippoBean = getHippoBean("/content/documents/mychannel/news/news1");
        hstLinkCreator.addHstLink(hippoBean.getNode().getParent().getIdentifier(), HST_LINK);
        testLinkViaBeanAndNodeAndUuid(hippoBean);
    }

    @Test
    public void addHstLinkViaSiteMap() throws RepositoryException {
        HstSiteMapItem mapItem = Mockito.mock(HstSiteMapItem.class);
        hstLinkCreator.addHstLink(mapItem, HST_LINK);
        HstLink hstLink = hstLinkCreator.create(mapItem, mount);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));
    }

    @Test
    public void addHstLinkViaRefId() throws RepositoryException {
        hstLinkCreator.addHstLinkByRefId("refId", HST_LINK);
        HstLink hstLink = hstLinkCreator.createByRefId("refId", mount);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));
    }

    @Test
    public void createHstLinkViaPath() throws RepositoryException {
        HstLink hstLink = hstLinkCreator.create("/some/path", mount);
        assertEquals("/some/path/", hstLink.toUrlForm(requestContext, true));
    }

    private void testLinkViaBeanAndNodeAndUuid(HippoBean hippoBean) throws RepositoryException {
        HstLink hstLink = hstLinkCreator.create(hippoBean, requestContext);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));

        hstLink = hstLinkCreator.create(hippoBean.getNode(), requestContext);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));

        hstLink = hstLinkCreator.create(getHandleNode(hippoBean), requestContext);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));

        hstLink = hstLinkCreator.create(getHandleNode(hippoBean).getIdentifier(), rootNode.getSession(), requestContext);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));

        hstLink = hstLinkCreator.createCanonical(getHandleNode(hippoBean), requestContext);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));

        hstLink = hstLinkCreator.createCanonical(hippoBean.getNode(), requestContext, null);
        assertEquals("https://www.openweb.nl/diensten", hstLink.toUrlForm(requestContext, true));

        List<HstLink> links = hstLinkCreator.createAllAvailableCanonicals(hippoBean.getNode(), requestContext);
        assertEquals("https://www.openweb.nl/diensten", links.get(0).toUrlForm(requestContext, true));
    }

    private Node getHandleNode(HippoBean hippoBean) throws RepositoryException {
        return hippoBean.getNode().getParent();
    }

    public void teardown() {
        super.teardown();
    }

    @Override
    protected String getAnnotatedClassesResourcePath() {
        return "classpath*:org/onehippo/forge/**/*.class, " +
                "classpath*:com/onehippo/**/*.class, " +
                "classpath*:org/onehippo/cms7/hst/beans/**/*.class, " +
                "classpath*:nl/openweb/hippo/test/domain/**/*.class";
    }

}