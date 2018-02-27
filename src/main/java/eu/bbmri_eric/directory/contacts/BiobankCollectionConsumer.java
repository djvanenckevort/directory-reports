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
public class BiobankCollectionConsumer implements EntityConsumer {

    private final Set<BiobankOrganisation> biobanks;
    private final Set<Contact> contacts;
    private final Set<BiobankNetwork> networks;
    private final Set<BiobankCollection> collections = new HashSet<>();

    public BiobankCollectionConsumer(Set<BiobankOrganisation> biobanks, Set<Contact> contacts, Set<BiobankNetwork> networks) {
        this.biobanks = biobanks;
        this.contacts = contacts;
        this.networks = networks;
    }

    @Override
    public void accept(Map<Attribute, String> t) {
        final String id = t.get(Attribute.from("id"));
        final String name = t.get(Attribute.from("name"));
        final String biobankID = t.get(Attribute.from("biobank"));
        int contactPrio = Integer.valueOf(t.get(Attribute.from("contact_priority")));
        final BiobankOrganisation biobank = biobanks.stream().filter(B -> biobankID.equals(B.getId())).findFirst().get();
        final Contact contact = getContact(t.get(Attribute.from("contact")));
        final String parentID = t.getOrDefault(Attribute.from("parent"), "");
        final BiobankCollection parent = collections.stream().filter(C -> parentID.equals(C.getId())).findFirst().orElse(null);
        final List<String> networkIDs = Arrays.asList(t.getOrDefault(Attribute.from("networks"), "").split(","));
        final List<BiobankNetwork> nets = networks.stream().filter(N -> networkIDs.contains(N.getId())).collect(Collectors.toList());
        final int maxNetworkContactPriority = nets.stream().max(new PriorityComparator()).map(N -> N.getContactPriority()).orElse(0);
        if (maxNetworkContactPriority == contactPrio && contactPrio > biobank.getContactPriority()) {
            System.err.println("Contact priority for the collection and network is equal, ignoring networks to break the tie.");
            final BiobankNetwork network = nets.stream().max(new PriorityComparator()).get();
            System.err.println(String.format("Priority: %d; Network: %s  Biobank: %s", contactPrio, network.getId(), id));
            nets.clear();       
        } else if (contactPrio == biobank.getContactPriority()) {
            System.err.println("Contact priority for the collection and biobank is equal, raising collection priority to break the tie.");
            System.err.println(String.format("Priority: %d; Collection: %s  Biobank: %s", contactPrio, id, biobank.getId()));
            contactPrio++;
        }
        final BiobankCollection collection = new BiobankCollection(id, name, biobank, contact, nets, contactPrio, parent);
        collections.add(collection);
    }

    public Set<BiobankCollection> getCollections() {
        return collections;
    }

    public Contact getContact(String id) {
        String contactID = id.split(",")[0];
        return contacts.stream().filter(C -> contactID.equals(C.getId())).findFirst().get();
    }
    private static class PriorityComparator implements Comparator<BiobankNetwork> {

        @Override
        public int compare(BiobankNetwork lhs, BiobankNetwork rhs) {
            return lhs.getContactPriority() - rhs.getContactPriority();
        }
        
    }

}
