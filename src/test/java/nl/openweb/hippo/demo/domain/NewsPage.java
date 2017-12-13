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
package nl.openweb.hippo.demo.domain;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;

/**
 * @author Ebrahim Aharpour
 * @since 9/9/2017
 */
@Node(jcrType = "ns:NewsPage")
public class NewsPage extends HippoDocument {

    public String getTitle() {
        return getProperty("ns:title");
    }

    public String getIntroduction() {
        return getProperty("ns:introduction");
    }

    public String getSubjecttags() {
        return getProperty("ns:subjecttags");
    }

    public String getBrowserTitle() {
        return getProperty("ns:browserTitle");
    }

    public NewsPage getRelatedNews() {
        return getLinkedBean("ns:relatedNews", NewsPage.class);
    }

}
