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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author david
 */
public class ContactReport {

    private final Set<BiobankCollection> collections;
    private final Map<Contact, List<BiobankCollection>> contactToCollection;

    public ContactReport(Set<BiobankCollection> collections) {
        this.collections = collections;
        contactToCollection = new HashMap<>();
    }

    public void create(final Path file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createCollectionToContactsSheet(workbook);
            createContactToCollectionSheet(workbook);
            workbook.write(Files.newOutputStream(file));
        }
    }

    private void createCollectionToContactsSheet(final XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("collection_to_contact");
        int rowno = 0;
        for (BiobankCollection collection : collections) {
            Set<Tuple<Integer, Contact>> candidates = getCollectionContacts(collection);
            XSSFRow row = sheet.createRow(rowno);
            row.createCell(0).setCellValue(collection.getId());
            Contact contact = getContactWithHighestPriority(candidates);
            row.createCell(1).setCellValue(contact.getId());
            row.createCell(2).setCellValue(contact.getEmail());
            rowno += 1;
            List<BiobankCollection> list = contactToCollection.getOrDefault(contact, new ArrayList<>());
            list.add(collection);
            contactToCollection.put(contact, list);

        }
    }

    private void createContactToCollectionSheet(final XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("contact_to_collection");
        int rowno = 0;
        for (Contact contact : contactToCollection.keySet()) {
            List<BiobankCollection> list = contactToCollection.get(contact);
            XSSFRow row = sheet.createRow(rowno);
            row.createCell(0).setCellValue(contact.getId());
            row.createCell(1).setCellValue(contact.getEmail());
            List<BiobankCollection> sorted = list.stream().sorted((o1, o2) -> o1.getBiobank().getId().compareTo(o2.getBiobank().getId())).collect(Collectors.toList());
            BiobankCollection previous = sorted.get(0);
            StringBuilder builder = new StringBuilder();
            for (BiobankCollection c : sorted) {
                if (!previous.getBiobank().getId().equals(c.getBiobank().getId())) {
                    builder.append(String.format("hosted in %s (%s)\n\n", previous.getId(), previous.getName()));
                }
                builder.append(String.format("%s (%s)\n", c.getId(), c.getName()));
            }
            builder.append(String.format("hosted in %s (%s)", previous.getId(), previous.getName()));
            row.createCell(2).setCellValue(builder.toString());
            rowno += 1;
        }
    }

    private Set<Tuple<Integer, Contact>> getCollectionContacts(BiobankCollection collection) {
        Set<Tuple<Integer, Contact>> candidates = new HashSet<>();
        candidates.add(new Tuple(collection.getContactPriority(), collection.getContact()));
        candidates.addAll(getBiobankContacts(collection.getBiobank()));
        collection.getNetworks().forEach(network -> candidates.addAll(getNetworkContacts(network)));

        BiobankCollection parent = collection.getParent();
        while (parent != null) {
            candidates.addAll(getCollectionContacts(parent));
            parent = parent.getParent();
        }
        return candidates;
    }

    private Set<Tuple<Integer, Contact>> getBiobankContacts(final BiobankOrganisation biobank) {
        Set<Tuple<Integer, Contact>> candidates = new HashSet<>();
        candidates.add(new Tuple(biobank.getContactPriority(), biobank.getContact()));
        biobank.getNetworks().forEach(network -> candidates.addAll(getNetworkContacts(network)));
        return candidates;
    }

    private Collection<? extends Tuple<Integer, Contact>> getNetworkContacts(final BiobankNetwork network) {
        Set<Tuple<Integer, Contact>> candidates = new HashSet<>();
        candidates.add(new Tuple(network.getContactPriority(), network.getContact()));
        BiobankNetwork parent = network.getParent();
        while (parent != null) {
            candidates.addAll(getNetworkContacts(parent));
            parent = parent.getParent();
        }
        return candidates;
    }

    private Contact getContactWithHighestPriority(Set<Tuple<Integer, Contact>> candidates) {
        return candidates.stream().reduce(new Tuple<>(-1, null), (t, u) -> {
            int first = t.getFirst();
            int second = u.getFirst();
            if (first > second) {
                return t;
            }
            return u;
        }).getSecond();
    }

}
