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

import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.hippo.importer.YamlImporter;
import nl.openweb.jcr.InMemoryJcrRepository;
import nl.openweb.jcr.importer.JcrImporter;
import nl.openweb.jcr.importer.JsonImporter;
import nl.openweb.jcr.importer.XmlImporter;

import javax.jcr.*;
import java.io.IOException;

/**
 * @author Ebrahim Aharpour
 * @since 8/27/2017
 */
public abstract class BaseHippoTest extends AbstractRepoTest {

    public static final SimpleCredentials ADMIN = new SimpleCredentials("admin", "admin".toCharArray());
    private InMemoryJcrRepository repository;


    @Override
    public void setup() {
        repository = createRepository();
        rootNode = getRootNode();
        super.setup();
        componentManager.addComponent(Repository.class.getName(), repository);
        componentManager.addComponent(Credentials.class.getName() + ".hstconfigreader", getWritableCredentials());
        componentManager.addComponent(Credentials.class.getName() + ".default", getWritableCredentials());
        componentManager.addComponent(Credentials.class.getName() + ".binaries", getWritableCredentials());
        componentManager.addComponent(Credentials.class.getName() + ".preview", getWritableCredentials());
        componentManager.addComponent(Credentials.class.getName() + ".writable", getWritableCredentials());
    }

    protected InMemoryJcrRepository createRepository() {
        try {
            return new InMemoryJcrRepository();
        } catch (RepositoryException| IOException e) {
            throw new SetupTeardownException(e);
        }
    }

    private Node getRootNode() {
            try {
                if (repository == null) {
                    repository = createRepository();
                }
                Session session = repository.login(ADMIN);
                return  session.getRootNode();
            } catch (Exception e) {
                throw new SetupTeardownException(e);
            }
    }

    protected Credentials getWritableCredentials() {
        return ADMIN;
    }

    @Override
    protected JcrImporter getImporter(String fileFormat) {
        JcrImporter importer;
        switch (fileFormat) {
            case JsonImporter.FORMAT:
                importer = new JsonImporter(getRootNode());
                break;
            case XmlImporter.FORMAT:
                importer = new XmlImporter(getRootNode());
                break;
            case YamlImporter.FORMAT:
                importer = new YamlImporter(getRootNode());
                break;
            default:
                throw new IllegalArgumentException("No importer for fileFormat: " + fileFormat);
        }
        importer.addUnknownTypes(true)
                .saveSession(true)
                .addUuid(true);

        return importer;
    }

    @Override
    public void teardown() {
        super.teardown();
        repository.close();
    }
}
