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
package nl.openweb.hippo.test;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onehippo.cms7.essentials.components.EssentialsListComponent;
import org.onehippo.cms7.essentials.components.info.EssentialsListComponentInfo;
import org.onehippo.cms7.essentials.components.paging.IterablePagination;

import nl.openweb.hippo.BaseHippoTest;
import nl.openweb.hippo.test.domain.AnotherType;
import nl.openweb.hippo.test.domain.NewsPage;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ebrahim Aharpour
 * @since 9/9/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class EssentialsListComponentTest extends BaseHippoTest {

    private EssentialsListComponent component = new EssentialsListComponent();

    @Before
    public void setup() {
        super.setup();
        setSiteContentBase("/test-case-1");

        request.setParameterMap("", new HashMap<>());
        component.init(null, componentConfiguration);
    }

    @After
    public void teardown() {
        super.teardown();
    }


    @Override
    protected String getPathToTestResource() {
        return "news.xml";
    }

    @Override
    protected String getAnnotatedClassesResourcePath() {
        return "classpath*:org/onehippo/forge/**/*.class, " +
                "classpath*:com/onehippo/**/*.class, " +
                "classpath*:org/onehippo/cms7/hst/beans/**/*.class, " +
                "classpath*:nl/openweb/hippo/test/domain/**/*.class";
    }

    @Test
    public void ascending() throws RepositoryException {
        setParamInfo("", "ns:NewsPage", 5,
                "ns:releaseDate", "asc");
        component.doBeforeRender(request, response);
        IterablePagination<NewsPage> pageable = getRequestAttribute("pageable");
        List<NewsPage> items = pageable.getItems();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("news1", items.get(0).getName());
        Assert.assertEquals("news2", items.get(1).getName());
        Assert.assertEquals("news3", items.get(2).getName());
    }



    @Test
    public void descending() throws RepositoryException {
        setParamInfo("", "ns:NewsPage", 5,
                "ns:releaseDate", "desc");
        component.doBeforeRender(request, response);
        IterablePagination<NewsPage> pageable = getRequestAttribute("pageable");
        List<NewsPage> items = pageable.getItems();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("news3", items.get(0).getName());
        Assert.assertEquals("news2", items.get(1).getName());
        Assert.assertEquals("news1", items.get(2).getName());
    }

    @Test
    public void sortByTitle() throws RepositoryException {
        setParamInfo("", "ns:NewsPage", 5,
                "ns:title", "asc");
        component.doBeforeRender(request, response);
        IterablePagination<NewsPage> pageable = getRequestAttribute("pageable");
        List<NewsPage> items = pageable.getItems();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("news3", items.get(0).getName());
        Assert.assertEquals("news1", items.get(1).getName());
        Assert.assertEquals("news2", items.get(2).getName());
    }

    @Test
    public void pageSize() throws RepositoryException {
        setParamInfo("", "ns:NewsPage", 2,
                "ns:releaseDate", "asc");
        component.doBeforeRender(request, response);
        IterablePagination<NewsPage> pageable = getRequestAttribute("pageable");
        List<NewsPage> items = pageable.getItems();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("news1", items.get(0).getName());
        Assert.assertEquals("news2", items.get(1).getName());
    }

    @Test
    public void paging() throws RepositoryException {
        setParamInfo("", "ns:NewsPage", 2,
                "ns:releaseDate", "asc");
        request.addParameter("page", "2");
        component.doBeforeRender(request, response);
        IterablePagination<NewsPage> pageable = getRequestAttribute("pageable");
        List<NewsPage> items = pageable.getItems();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals("news3", items.get(0).getName());
    }

    @Test
    public void search() throws RepositoryException {
        setParamInfo("", "ns:NewsPage", 2,
                "ns:releaseDate", "asc");
        request.addParameter("query", "sapien");
        component.doBeforeRender(request, response);
        IterablePagination<NewsPage> pageable = getRequestAttribute("pageable");
        List<NewsPage> items = pageable.getItems();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals("news1", items.get(0).getName());
        Assert.assertEquals("Pellentesque sapien tellus, commodo luctus orci sed, " +
                        "auctor pretium nulla. Sed metus justo, placerat nec hendrerit elementum",
                items.get(0).getTitle());
    }

    @Test
    public void type() throws RepositoryException {
        setParamInfo("", "ns:AnotherType", 2,
                "ns:releaseDate", "asc");
        component.doBeforeRender(request, response);
        IterablePagination<AnotherType> pageable = getRequestAttribute("pageable");
        List<AnotherType> items = pageable.getItems();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals("another-type", items.get(0).getName());
    }


    private void setParamInfo(String path, String type, int pageSize, String sortField, String sortOrder) {
        EssentialsListComponentInfo paramInfo = mock(EssentialsListComponentInfo.class);
        when(paramInfo.getDocumentTypes()).thenReturn(type);
        when(paramInfo.getIncludeSubtypes()).thenReturn(false);
        when(paramInfo.getPath()).thenReturn(path);
        when(paramInfo.getPageSize()).thenReturn(pageSize);
        when(paramInfo.getShowPagination()).thenReturn(true);
        when(paramInfo.getSortField()).thenReturn(sortField);
        when(paramInfo.getSortOrder()).thenReturn(sortOrder);
        setComponentParameterInfo(paramInfo);
    }
}
