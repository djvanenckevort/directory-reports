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

import java.util.List;
import java.util.Objects;

/**
 *
 * @author david
 */
public class BiobankCollection {
    private final String id;
    private final String name;
    private final BiobankOrganisation biobank;
    private final Contact contact;
    private final List<BiobankNetwork> networks;
    private final int contactPriority;
    private final BiobankCollection parent;

    public BiobankCollection(String id, String name, BiobankOrganisation biobank, Contact contact, List<BiobankNetwork> networks, int contactPriority, BiobankCollection parent) {
        this.id = id;
        this.name = name;
        this.biobank = biobank;
        this.contact = contact;
        this.networks = networks;
        this.contactPriority = contactPriority;
        this.parent = parent;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.id);
        hash = 71 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BiobankCollection other = (BiobankCollection) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.name, other.name);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BiobankOrganisation getBiobank() {
        return biobank;
    }

    public Contact getContact() {
        return contact;
    }

    public List<BiobankNetwork> getNetworks() {
        return networks;
    }

    public int getContactPriority() {
        return contactPriority;
    }

    public BiobankCollection getParent() {
        return parent;
    }
    
    
}
