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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.internal.MutableResolvedMount;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.core.request.ResolvedVirtualHost;

/**
 * @author Ebrahim Aharpour
 * @since 6/19/2017
 */
public class MockResolvedMount implements MutableResolvedMount {
    private Mount mount;
    private ResolvedVirtualHost resolvedVirtualHost;
    private String resolvedMountPath;
    private String matchingIgnoredPrefix;
    private int portNumber;
    private Map<String, ResolvedSiteMapItem> sitemaps = new HashMap<>();

    public void addSitemap(String siteMapPathInfo, ResolvedSiteMapItem siteMapItem) {
        sitemaps.put(siteMapPathInfo, siteMapItem);
    }

    @Override
    public String getNamedPipeline() {
        return mount.getNamedPipeline();
    }

    @Override
    public boolean isAuthenticated() {
        return mount.isAuthenticated();
    }

    @Override
    public Set<String> getRoles() {
        return mount.getRoles();
    }

    @Override
    public Set<String> getUsers() {
        return mount.getUsers();
    }

    @Override
    public boolean isSubjectBasedSession() {
        return mount.isSubjectBasedSession();
    }

    @Override
    public boolean isSessionStateful() {
        return mount.isSessionStateful();
    }

    @Override
    public String getFormLoginPage() {
        return mount.getFormLoginPage();
    }

    @Override
    public Mount getMount() {
        return mount;
    }

    @Override
    public void setMount(Mount mount) {
        this.mount = mount;
    }

    @Override
    public ResolvedVirtualHost getResolvedVirtualHost() {
        return resolvedVirtualHost;
    }

    public void setResolvedVirtualHost(ResolvedVirtualHost resolvedVirtualHost) {
        this.resolvedVirtualHost = resolvedVirtualHost;
    }

    @Override
    public String getResolvedMountPath() {
        return resolvedMountPath;
    }

    public void setResolvedMountPath(String resolvedMountPath) {
        this.resolvedMountPath = resolvedMountPath;
    }

    @Override
    public String getMatchingIgnoredPrefix() {
        return matchingIgnoredPrefix;
    }

    @Override
    public ResolvedSiteMapItem matchSiteMapItem(String siteMapPathInfo) {
        return sitemaps.get(siteMapPathInfo);
    }

    public void setMatchingIgnoredPrefix(String matchingIgnoredPrefix) {
        this.matchingIgnoredPrefix = matchingIgnoredPrefix;
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
}
