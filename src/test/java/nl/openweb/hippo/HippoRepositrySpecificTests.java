/*
 * Copyright 2018 Open Web IT B.V. (https://www.openweb.nl/)
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

import nl.openweb.jcr.importer.JcrImporter;
import nl.openweb.jcr.importer.XmlImporter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetNavigation;
import org.junit.*;

import javax.jcr.RepositoryException;

/**
 * @author Ebrahim Aharpour
 * @since 4/7/2018
 */
public class HippoRepositrySpecificTests extends BaseHippoTest {

    @Before
    public void setupScenario() throws RepositoryException {
        super.setup();
        registerNodeType("ns:account", "hippo:compound");
        registerNodeType("ns:basedocument", "hippo:document");
        registerNodeType("ns:author", "ns:basedocument");
        registerNodeType("ns:blogpost", "ns:basedocument");
        JcrImporter importer = getImporter(XmlImporter.FORMAT);
        importer.createNodes(getResourceAsStream("/nl/openweb/hippo/blog.xml"),
                "/content/documents/mychannel/blog", "hippostd:folder");
        importer.createNodes(getResourceAsStream("/nl/openweb/hippo/blogFacets.xml"),
                "/content/documents/mychannel/blogFacets", "hippostd:folder");
        setSiteContentBase("/content/documents/mychannel");

        recalculateHippoPaths();
    }

    @Test
    public void canonicalUUID() {
        HippoBean blogFolder = requestContext.getSiteContentBaseBean().getBean("blog");
        Assert.assertEquals("caf6c0c2-7be2-4f43-9248-f2232d6df1fb", blogFolder.getCanonicalUUID());
        HippoDocument firstBlogPost = requestContext.getSiteContentBaseBean().getBean("blog/2018/04/first-blog-post");
        Assert.assertEquals("1ba60d37-3b6c-4d3b-be5a-5477d1f06f54", firstBlogPost.getCanonicalUUID());
        Assert.assertEquals("2cb6ced5-4003-4c3f-af3d-fe348e8c7735", firstBlogPost.getCanonicalHandleUUID());
    }

    @Test
    public void canonicalHandlePath() {
        HippoDocument firstBlogPost = requestContext.getSiteContentBaseBean().getBean("blog/2018/04/first-blog-post");
        Assert.assertEquals("/content/documents/mychannel/blog/2018/04/first-blog-post/first-blog-post", firstBlogPost.getCanonicalPath());
    }

    @Test
    @Ignore
    public void facetedNavigation() {
        HippoFacetNavigation facet = requestContext.getSiteContentBaseBean().getBean("blogFacets");
        Assert.assertEquals((Long) 2L, facet.getCount());
        Assert.assertEquals(2, facet.getDocumentSize());
        Assert.assertNotNull(facet.getResultSet());
    }

    @Override
    protected String getAnnotatedClassesResourcePath() {
        return "classpath*:org/onehippo/forge/**/*.class, " +
                "classpath*:com/onehippo/**/*.class, " +
                "classpath*:org/onehippo/cms7/hst/beans/**/*.class, " +
                "classpath*:nl/openweb/hippo/demo/domain/**/*.class";
    }

    @After
    public void teardown() {
        super.teardown();
    }
}
