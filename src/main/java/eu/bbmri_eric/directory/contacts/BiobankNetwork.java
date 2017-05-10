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

import java.util.Objects;

/**
 *
 * @author david
 */
public class BiobankNetwork {

    private final String name;
    private final String id;
    private final Contact contact;
    private final BiobankNetwork parent;
    private final int contactPriority;

    public BiobankNetwork(String id, String name, Contact contact, BiobankNetwork parent, int contactPriority) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.parent = parent;
        this.contactPriority = contactPriority;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.id);
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
        final BiobankNetwork other = (BiobankNetwork) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.id, other.id);
    }

    public String getName() {
        return name;
    }
    
    public String getId() {
        return id;
    }

    public Contact getContact() {
        return contact;
    }

    public BiobankNetwork getParent() {
        return parent;
    }

    public int getContactPriority() {
        return contactPriority;
    }
    
}
