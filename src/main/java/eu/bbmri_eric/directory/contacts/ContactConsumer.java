/*
 * Copyright 2017 david.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.bbmri_eric.directory.contacts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.molgenis.downloader.api.EntityConsumer;
import org.molgenis.downloader.api.metadata.Attribute;

/**
 *
 * @author david
 */
public class ContactConsumer implements EntityConsumer {
    
    private final Set<Contact> contacts = new HashSet<>();

    public Set<Contact> getContacts() {
        return contacts;
    }

    @Override
    public void accept(Map<Attribute, String> t) {
        Attribute id = Attribute.from("id");
        Attribute name = Attribute.from("name");
        Attribute email = Attribute.from("email");
        Contact contact = new Contact(t.get(id), t.get(name), t.get(email));
        contacts.add(contact);
    }
    
}
