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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.downloader.api.EntityConsumer;
import org.molgenis.downloader.api.metadata.Attribute;

/**
 *
 * @author david
 */
public class BiobankOrganisationConsumer implements EntityConsumer {

    private final Set<Contact> contacts;
    private final Set<BiobankNetwork> networks;
    private final Set<BiobankOrganisation> biobanks = new HashSet<>();

    public BiobankOrganisationConsumer(Set<Contact> contacts, Set<BiobankNetwork> networks) {
        this.contacts = contacts;
        this.networks = networks;
    }

    @Override
    public void accept(Map<Attribute, String> biobank) {
        final String id = biobank.get(Attribute.from("id"));
        final String name = biobank.get(Attribute.from("name"));
        final String contactId = biobank.get(Attribute.from("contact"));
        final Contact contact;
        if (contactId != null) {
            contact = getContact(contactId);
        } else {
            System.err.println(String.format("Contact is null for %s (%s)", name, id));
            contact = null;
        }
        final List<String> networkIDs = Arrays.asList(biobank.getOrDefault(Attribute.from("networks"), "").split(","));
        final List<BiobankNetwork> nets = networks.stream().filter(N -> networkIDs.contains(N.getId())).collect(Collectors.toList());
        int contactPriority = Integer.valueOf(biobank.get(Attribute.from("contact_priority")));
        final int maxNetworkContactPriority = nets.stream().max(new PriorityComparator()).map(N -> N.getContactPriority()).orElse(0);
        if (contactPriority == maxNetworkContactPriority && contactPriority > 0) {
            System.err.println("Contact priority for the biobank and network is equal, ignoring networks to break the tie.");
            final BiobankNetwork network = nets.stream().max(new PriorityComparator()).get();
            System.err.println(String.format("Priority: %d; Network: %s  Biobank: %s", contactPriority, network.getId(), id));
            nets.clear();
        }
        final BiobankOrganisation organisation = new BiobankOrganisation(id, name, contact, nets, contactPriority);
        biobanks.add(organisation);
    }

    public Set<BiobankOrganisation> getBiobanks() {
        return biobanks;
    }
    
    public Contact getContact(String id) {
        return contacts.stream().filter(C -> id.equals(C.getId())).findFirst().get();
    }
    
    private static class PriorityComparator implements Comparator<BiobankNetwork> {

        @Override
        public int compare(BiobankNetwork lhs, BiobankNetwork rhs) {
            return lhs.getContactPriority() - rhs.getContactPriority();
        }
        
    }
}
