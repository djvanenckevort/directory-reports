/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.downloader.api.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author david
 */
public final class Attribute implements Metadata {
    private Entity entity;
    private String id;
    private String name;
    private DataType dataType;
    private Entity refEntity;
    private boolean idAttribute;
    private boolean lookupAttribute;
    private boolean optional;
    private boolean auto;
    private boolean visible;
    private boolean readOnly;
    private boolean unique;
    private boolean aggregatable;
    private boolean labelAttribute;
    private String enumOptions;
    private String expression;
    private String label;
    private String description;
    private String visibleExpression;
    private String validationExpression;
    private String defaultValue;
    private String orderBy;
    private Attribute mappedBy;
    private Integer rangeMin;
    private Integer rangeMax;
    private final Set<Tag> tags;
    private final Map<Language, String> labels;
    private final Map<Language, String> descriptions;
    private final List<Attribute> parts;
    private Attribute compound;

    public Attribute(final String id) {
        this.id = id;
        labels = new HashMap<>();
        descriptions = new HashMap<>();
        tags = new HashSet<>();
        parts = new ArrayList<>();        
        visible = true;
        dataType = DataType.STRING;
    }
    
    public static Attribute from(final String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        final Attribute att = new Attribute(id);
        att.setName(id);
        return att;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.id);
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
        final Attribute other = (Attribute) obj;
        return Objects.equals(this.id, other.id);
    }

    public Attribute getMappedBy() {
        return mappedBy;
    }

    public void setMappedBy(Attribute mappedBy) {
        this.mappedBy = mappedBy;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isLabelAttribute() {
        return labelAttribute;
    }

    public List<Attribute> getParts() {
        return parts;
    }

    public Attribute getCompound() {
        return compound;
    }

    public Entity getEntity() {
        return entity;
    }

    public Entity getRefEntity() {
        return refEntity;
    }

    public boolean isIdAttribute() {
        return idAttribute;
    }

    public boolean isLookupAttribute() {
        return lookupAttribute;
    }

    public String getId() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public boolean isAuto() {
        return auto;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isHidden() {
        return !visible;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAggregatable() {
        return aggregatable;
    }

    public Integer getRangeMin() {
        return rangeMin;
    }

    public Integer getRangeMax() {
        return rangeMax;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isUnique() {
        return unique;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public String getVisibleExpression() {
        return visibleExpression;
    }

    public String getValidationExpression() {
        return validationExpression;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Map<Language, String> getLabels() {
        return labels;
    }

    public Map<Language, String> getDescriptions() {
        return descriptions;
    }

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getEnumOptions() {
        return enumOptions;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public void setRefEntity(Entity refEntity) {
        this.refEntity = refEntity;
    }

    public void setIdAttribute(boolean idAttribute) {
        this.idAttribute = idAttribute;
    }

    public void setLookupAttribute(boolean lookupAttribute) {
        this.lookupAttribute = lookupAttribute;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setAggregatable(boolean aggregatable) {
        this.aggregatable = aggregatable;
    }

    public void setLabelAttribute(boolean labelAttribute) {
        this.labelAttribute = labelAttribute;
    }

    public void setEnumOptions(String enumOptions) {
        this.enumOptions = enumOptions;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVisibleExpression(String visibleExpression) {
        this.visibleExpression = visibleExpression;
    }

    public void setValidationExpression(String validationExpression) {
        this.validationExpression = validationExpression;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setRangeMin(Integer rangeMin) {
        this.rangeMin = rangeMin;
    }

    public void setRangeMax(Integer rangeMax) {
        this.rangeMax = rangeMax;
    }

    public void setCompound(Attribute compound) {
        this.compound = compound;
    }
    
    public void addPart(final Attribute part) {
        parts.add(part);
    }
    
    public void addTag(final Tag tag) {
        tags.add(tag);
    }
    
    public void addDescription(final String description, final Language language) {
        descriptions.put(language, description);
    }
    
    public void addLabel(final String label, final Language language) {
        labels.put(language, label);
    }
}
