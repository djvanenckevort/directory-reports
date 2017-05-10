
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
public class BiobankOrganisation {
    private final String id;
    private final String name;
    private final Contact contact;
    private final List<BiobankNetwork> networks;
    private final int contactPriority;

    public BiobankOrganisation(String id, String name, Contact contact, List<BiobankNetwork> networks, int contactPriority) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.networks = networks;
        this.contactPriority = contactPriority;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.id);
        hash = 59 * hash + Objects.hashCode(this.name);
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
        final BiobankOrganisation other = (BiobankOrganisation) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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
}
