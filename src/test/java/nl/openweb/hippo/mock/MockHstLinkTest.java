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

import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Ebrahim Aharpour
 * @since 11/29/2017
 */
public class MockHstLinkTest {

    public static final String BASE_URL = "https://www.openweb.nl";

    @Test
    public void toUrlForm() throws Exception {
        verifyCaseWithoutSubpath("/", null);
        verifyCaseWithoutSubpath("/", "");
        verifyCaseWithoutSubpath("/", "/");
        verifyCaseWithoutSubpath("/path", "path");
        verifyCaseWithoutSubpath("/path", "/path");
        verifyCaseWithoutSubpath("/path", "path/");
        verifyCaseWithoutSubpath("/path", "/path/");

        MockHstLink link = new MockHstLink(BASE_URL, "path", "subpath");
        assertEquals(BASE_URL + "/path./subpath", link.toUrlForm(null, true));
        assertEquals("/path./subpath", link.toUrlForm(null, false));
    }

    private void verifyCaseWithoutSubpath(String expected, String path) {
        MockHstLink link = new MockHstLink(BASE_URL, path);
        assertEquals(BASE_URL + expected, link.toUrlForm(null, true));
        assertEquals(expected, link.toUrlForm(null, false));
    }

}