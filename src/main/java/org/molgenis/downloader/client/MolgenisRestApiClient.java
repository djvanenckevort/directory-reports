/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.downloader.client;

import org.molgenis.downloader.api.metadata.MolgenisVersion;
import org.molgenis.downloader.api.EntityConsumer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.molgenis.downloader.api.MetadataConsumer;
import org.molgenis.downloader.api.MolgenisClient;
import org.molgenis.downloader.api.metadata.Attribute;
import org.molgenis.downloader.api.metadata.DataType;
import org.molgenis.downloader.api.metadata.Entity;

/**
 *
 * @author david
 */
public class MolgenisRestApiClient implements MolgenisClient {

    private final HttpClient client;
    private final MetadataRepository repository;
    private final MetadataConverter converter;
    private final URI uri;
    private String token;

    public MolgenisRestApiClient(final HttpClient client, final URI uri) throws IOException, URISyntaxException {
        this.client = client;
        this.uri = uri;
        final MolgenisVersion version = version();
        repository = new MetadataRepository();
        MolgenisVersion VERSION_2 = new MolgenisVersion(2, 0, 0);
        if (version.smallerThan(VERSION_2)) {
            converter = new MolgenisV1MetadataConverter(repository);
        } else {
            converter = new MolgenisV2MetadataConverter(repository);
        }
    }

    @Override
    public final boolean login(final String username, final String password) {
        final JSONObject login = new JSONObject();
        login.put("username", username);
        login.put("password", password);
        JSONObject response;
        token = null;
        try {
            final HttpPost request = new HttpPost(new URI(uri + "/api/v1/login"));
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(login.toString()));

            final HttpResponse result = client.execute(request);
            if (result.getStatusLine().getStatusCode() == 200) {
                response = new JSONObject(EntityUtils.toString(result.getEntity()));
                token = response.getString("token");
            }
        } catch (final JSONException | IOException | URISyntaxException ex) {
            System.console().format("An error occurred while logging in: %s.\n", ex.getLocalizedMessage()).flush();
        }
        return token != null;
    }

    @Override
    public final boolean logout() {
        HttpGet request = new HttpGet(uri + "/api/v1/logout");
        if (token != null) {
            request.setHeader("x-molgenis-token", token);
        }
        try {
            return client.execute(request).getStatusLine().getStatusCode() == 200;
        } catch (IOException ex) {
            System.console().format("An error occurred while logging out: %s.\n", ex.getLocalizedMessage()).flush();
            return false;
        }
    }

    @Override
    public final MolgenisVersion version() throws IOException, URISyntaxException {
        MolgenisVersion version;
        try {
            version = MolgenisVersion.from(download(new URI(uri + "/api/v2/version")).getString("molgenisVersion"));
        } catch (IOException | URISyntaxException | ParseException | JSONException ex) {
            System.console().format("Failed to retrieve MOLGENIS version, assuming 1.0.0.\n%s.\n", ex.getLocalizedMessage()).flush();
            version = MolgenisVersion.from("1.0.0");
        }
        return version;
    }

    @Override
    public Entity getEntity(final String name) throws IOException, URISyntaxException {
        return getEntity(name, null);
    }

    @Override
    public Entity getEntity(String name, List<Attribute> attributes) throws IOException, URISyntaxException {
        final JSONObject json;
        if (attributes == null || attributes.isEmpty()) {
            json = download(new URI(uri + "/api/v2/" + name));
        } else {
            final String attrs = attributes.stream().map(Attribute::getName).collect(Collectors.joining(","));
            json = download(new URI(uri + "/api/v2/" + name + "?attrs=" + attrs));
        }
        final JSONObject meta = json.getJSONObject("meta");
        return entityFromJSON(meta);
    }

    @Override
    public final void streamEntityData(final String name, final EntityConsumer consumer) {
        streamEntityData(name, null, consumer);
    }

    @Override
    public final void streamEntityData(final String name, final List<Attribute> attributes, final EntityConsumer consumer) {

        try {
            JSONObject json;
            if (attributes == null || attributes.isEmpty()) {
                json = download(new URI(uri + "/api/v2/" + name));
            } else {
                final String attrs = attributes.stream().map(Attribute::getName).collect(Collectors.joining(","));
                json = download(new URI(uri + "/api/v2/" + name + "?attrs=" + attrs));
            }

            final JSONObject meta = json.getJSONObject("meta");
            final Entity entity = entityFromJSON(meta);

            String nextHref = null;
            do {
                final JSONArray items = json.getJSONArray("items");
                items.iterator().forEachRemaining((Object item) -> consumer.accept(getAttributes((JSONObject) item, entity.getAttributes())));

                nextHref = json.optString("nextHref");
                if (notNullOrEmpty(nextHref)) {
                    json = download(new URI(nextHref));
                }
            } while (notNullOrEmpty(nextHref));

        } catch (final JSONException | IOException | URISyntaxException | ParseException ex) {
            Logger.getLogger(MolgenisRestApiClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void getMetaData(MetadataConsumer consumer) {
        streamEntityData(converter.getLanguagesRepository(), converter::toLanguage);
        streamEntityData(converter.getTagsRepository(), converter::toTag);
        streamEntityData(converter.getPackagesRepository(), converter::toPackage);
        streamEntityData(converter.getAttributesRepository(), converter::toAttribute);
        streamEntityData(converter.getEnitiesRepository(), converter::toEntity);
        consumer.accept(repository);
    }

    @Override
    public void close() throws Exception {
        try {
            if (token != null) {
                logout();
            }
        } finally {
            token = null;
        }
    }

    private JSONObject download(final URI uri) throws JSONException, IOException, ParseException {
        HttpGet request = new HttpGet(uri);
        if (token != null) {
            request.setHeader("x-molgenis-token", token);
        }
        HttpResponse result = client.execute(request);
        String data = EntityUtils.toString(result.getEntity(), StandardCharsets.UTF_8);
        return new JSONObject(data);
    }

    private boolean notNullOrEmpty(final String string) {
        return string != null && !"".equals(string);
    }

    private Map<Attribute, String> getAttributes(final JSONObject input, Collection<Attribute> attributes) {
        final Map<Attribute, String> data = new HashMap<>();

        attributes.forEach((Attribute attribute) -> {

            final DataType type = attribute.getDataType();
            final String name = attribute.getName();

            if (type.isXReferenceType()) {
                final JSONObject reference = input.optJSONObject(name);
                if (reference != null) {
                    final Entity refEntity = attribute.getRefEntity();
                    final String id = refEntity.getIdAttribute().getName();
                    if (refEntity.getIdAttribute().getDataType().isNumericType()) {
                        data.put(attribute, Long.toString(reference.getLong(id)));
                    } else {
                        data.put(attribute, reference.getString(id));
                    }
                }
            } else if (type.isMReferenceType()) {
                final JSONArray array = input.optJSONArray(name);
                if (array != null) {
                    final Entity refEntity = attribute.getRefEntity();
                    final String id = refEntity.getIdAttribute().getName();
                    final List<String> elements = new ArrayList<>();
                    array.forEach((Object element) -> {
                        final JSONObject reference = (JSONObject) element;
                        if (refEntity.getIdAttribute().getDataType().isNumericType()) {
                            elements.add(Long.toString(reference.getLong(id)));
                        } else {
                            elements.add(reference.getString(id));
                        }
                    });
                    final String references = elements.stream().collect(Collectors.joining(","));
                    data.put(attribute, references);
                }
            } else if (type.equals(DataType.COMPOUND)) {
                Map<Attribute, String> parts = getAttributes(input, attribute.getParts());
                data.putAll(parts);
            } else {
                final String value = input.optString(name);
                data.put(attribute, value);
            }
        });
        return data;
    }

    private Entity entityFromJSON(final JSONObject metadata) {
        Entity ent = new Entity(metadata.getString("name"));
        final String idAttribute = metadata.getString("idAttribute");
        final JSONArray attributes = metadata.getJSONArray("attributes");
        attributes.iterator().forEachRemaining((Object object) -> {
            final JSONObject attributeMetadata = (JSONObject) object;
            final Attribute attribute = attributeFromJSON(ent, attributeMetadata);
            ent.addAttribute(attribute);
            if (idAttribute.equals(attribute.getName())) {
                ent.setIdAttribute(attribute);
            }
        });
        return ent;
    }

    private Attribute attributeFromJSON(final Entity entity, final JSONObject meta) throws JSONException {
        final DataType type = DataType.valueOf(meta.getString("fieldType"));
        final String name = meta.getString("name");
        Attribute att = new Attribute(name);
        att.setEntity(entity);
        att.setName(name);
        att.setDataType(type);

        if (DataType.COMPOUND.equals(type)) {
            final JSONArray parts = meta.getJSONArray("attributes");
            parts.forEach(part -> {
                final JSONObject partMeta = (JSONObject) part;
                att.addPart(attributeFromJSON(entity, partMeta));
            });
        } else {
            final boolean optional = meta.getBoolean("nillable");
            att.setOptional(optional);
            if (type.isReferenceType()) {
                final JSONObject refEntity = meta.getJSONObject("refEntity");
                att.setRefEntity(entityFromJSON(refEntity));
            }
        }
        return att;
    }
}
