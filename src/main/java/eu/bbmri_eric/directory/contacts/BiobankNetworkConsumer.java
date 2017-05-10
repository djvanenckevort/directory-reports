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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.downloader.api.EntityConsumer;
import org.molgenis.downloader.api.metadata.Attribute;

/**
 *
 * @author david
 */
public class BiobankNetworkConsumer implements EntityConsumer {
    
    private final Set<BiobankNetwork> networks = new HashSet<>();
    private final Set<Contact> contacts;

    public BiobankNetworkConsumer(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    @Override
    public void accept(Map<Attribute, String> t) {
        final String id = t.get(Attribute.from("id"));
        final String name = t.get(Attribute.from("name"));
        final Contact contact = getContact(t.get(Attribute.from("contact")));
        final List<String> networkIDs = Arrays.asList(t.getOrDefault(Attribute.from("networks"), "").split(","));
        final BiobankNetwork parent = networks.stream().filter(N -> networkIDs.contains(N.getId())).findFirst().orElse(null);
        final int contactPriority = Integer.valueOf(t.get(Attribute.from("contact_priority")));
        final BiobankNetwork network = new BiobankNetwork(id, name, contact, parent, contactPriority);
        networks.add(network);
    }

    public Set<BiobankNetwork> getNetworks() {
        return networks;
    }

    public Contact getContact(String id) {
        String contactID = id.split(",")[0];
        return contacts.stream().filter(C -> contactID.equals(C.getId())).findFirst().get();
    }
}
