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

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.util.PathEncoder;


import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hippoecm.hst.util.PathUtils.FULLY_QUALIFIED_URL_PREFIXES;

/**
 * @author Ebrahim Aharpour
 * @since 11/29/2017
 */
public class MockHstLink extends org.hippoecm.hst.mock.core.linking.MockHstLink {

    private static final String SLASH = "/";
    private final String baseUrl;


    public MockHstLink(String baseUrl) {
        this(baseUrl, EMPTY);
    }

    public MockHstLink(String baseUrl, String path) {
        this(baseUrl, path, null);
    }

    public MockHstLink(String baseUrl, String path, String subpath) {
        super(path);
        setSubPath(subpath);
        this.baseUrl = baseUrl.endsWith(SLASH) ? baseUrl : baseUrl + SLASH;
    }

    @Override
    public String toUrlForm(HstRequestContext requestContext, boolean fullyQualified) {
        String result = handleFullyQualifiedPathCase();
        if (result == null) {
            result = (fullyQualified ? baseUrl : SLASH) + getNormalizedPath();
            if (StringUtils.isNotBlank(getSubPath())) {
                result = result + "./" + normalize(getSubPath());
            }
        }
        return result;
    }

    private String getNormalizedPath() {
        return normalize(getPath() != null ? getPath() : EMPTY);
    }

    private String handleFullyQualifiedPathCase() {
        try {
            String result = null;
            for (String prefix : FULLY_QUALIFIED_URL_PREFIXES) {
                if (getPath() != null && getPath().startsWith(prefix)) {
                    result = PathEncoder.encode(getPath(), UTF_8, FULLY_QUALIFIED_URL_PREFIXES);
                }
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new HstComponentException(e);
        }
    }

    private String normalize(String path) {
        String result = path;
        if (StringUtils.isNotBlank(path)) {
            int endIndex = path.endsWith(SLASH) ? path.length() - 1 : path.length();
            int beginIndex = path.startsWith(SLASH) ? 1 : 0;
            result = path.substring(beginIndex, Math.max(beginIndex, endIndex));
        }
        return result;
    }
}
