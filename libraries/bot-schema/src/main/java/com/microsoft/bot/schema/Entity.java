// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Metadata object pertaining to an activity.
 */
public class Entity {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();

    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    /**
     * Performs a deep copy of an Entity.
     * @param entity The Entity to clone.
     * @return The cloned Entity.
     */
    public static Entity clone(Entity entity) {
        if (entity == null) {
            return null;
        }

        return new Entity() {{
            setType(entity.getType());

            for (String key : entity.getProperties().keySet()) {
                setProperties(key, entity.getProperties().get(key));
            }
        }};
    }

    /**
     * Performs a deep copy of a List of Entities.
     * @param entities The List of Entities to clone.
     * @return A cloned List.
     */
    public static List<Entity> cloneList(List<Entity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(entity -> Entity.clone(entity))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Type of this entity (RFC 3987 IRI).
     * @return the type value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Type of this entity (RFC 3987 IRI).
     * @param withType the type value to set
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * Additional properties not defined by this class.
     * @return The Map of additional properties.
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * Additional properties not defined by this class.
     * @param key The key name of the property to set.
     * @param value The value of the property.
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }

    /**
     * Converts Entity to other Entity types.
     *
     * @param classType Class extended EntitySerialization
     * @param <T> The type of the return value.
     * @return Entity converted to type T
     */
    @JsonIgnore
    public <T extends EntitySerialization> T getAs(Class<T> classType) {

        // Serialize
        String tempJson;
        try {
            tempJson = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        // Deserialize
        T newObj = null;
        try {
            newObj = objectMapper.readValue(tempJson, classType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return newObj;
    }

    /**
     * Converts other Entity types to Entity.
     *
     * This is only intended to be used with other Entity classes:
     * @see Mention
     * @see Place
     * @see GeoCoordinates
     *
     * @param obj of type T
     * @param <T> The type of the value.
     * @return This Entity with the properties from the passed sub-Entity.
     */
    @JsonIgnore
    public <T extends EntitySerialization> Entity setAs(T obj) {
        // Serialize
        String tempJson;
        try {
            tempJson = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

        Entity tempEntity;
        try {
            tempEntity = objectMapper.readValue(tempJson, Entity.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        for (Map.Entry<String, JsonNode> entry : tempEntity.properties.entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }

        this.type = tempEntity.getType();

        return this;
    }
}
