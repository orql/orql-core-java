package com.github.orql.core.schema;

import com.github.orql.core.annotation.*;
import com.github.orql.core.exception.TypeNotSupportException;
import com.github.orql.core.util.Strings;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class SchemaManager {

    private static class ReflectWrapper {
        public SchemaInfo schema;
        public Field[] fields;
    }

    private static final Logger logger = LoggerFactory.getLogger(SchemaManager.class);

    private Map<String, SchemaInfo> schemas = new HashMap<>();

    /**
     * 表名下划线
     */
    private boolean tableUnderscore = false;

    /**
     * 字段下划线
     */
    private boolean fieldUnderscore = false;

    public void tableUnderscore(boolean tableUnderscore) {
        this.tableUnderscore = tableUnderscore;
    }

    public void fieldUnderscore(boolean fieldUnderscore) {
        this.fieldUnderscore = fieldUnderscore;
    }

    public SchemaManager addSchema(SchemaInfo schema) {
        schemas.put(schema.getName(), schema);
        return this;
    }

    public boolean containsSchema(String name) {
        return schemas.containsKey(name);
    }

    public SchemaInfo getSchema(String name) {
        if (schemas.containsKey(name)) {
            return schemas.get(name);
        }
        return null;
    }

    public SchemaInfo getSchema(Class clazz) {
        for (Map.Entry<String, SchemaInfo> entry : schemas.entrySet()) {
            if (entry.getValue().getClazz() == clazz) {
                return entry.getValue();
            }
        }
        return null;
    }

    public SchemaInfo getSchema(Object instance) {
        return getSchema(instance.getClass());
    }

    public void scanPackage(String path) {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(path));
        List<ReflectWrapper> reflectWrappers = new ArrayList<>();
        Set<Class<?>> schemas = reflections.getTypesAnnotatedWith(com.github.orql.core.annotation.Schema.class);
        for (Class<?> clazz : schemas) {
            logger.info("scan schema {}", clazz.getName());
            SchemaInfo schema = initSchema(clazz);
            Field[] fields = clazz.getDeclaredFields();
            ReflectWrapper wrapper = new ReflectWrapper();
            wrapper.schema = schema;
            wrapper.fields = fields;
            reflectWrappers.add(wrapper);
            addSchema(schema);
            initColumns(schema, fields);
        }
        for (ReflectWrapper wrapper : reflectWrappers) {
            initAssociations(wrapper.schema, wrapper.fields);
        }
    }

    private SchemaInfo initSchema(Class<?> clazz) {
        com.github.orql.core.annotation.Schema schemaAnnotation = clazz.getAnnotation(com.github.orql.core.annotation.Schema.class);
        SchemaInfo.Builder schemaBuilder = new SchemaInfo.Builder();
        if (!schemaAnnotation.value().equals("")) {
            schemaBuilder.name(schemaAnnotation.value());
        } else if (!schemaAnnotation.name().equals("")) {
            schemaBuilder.name(schemaAnnotation.name());
        } else {
            schemaBuilder.name(Strings.toLowerCaseFirst(clazz.getSimpleName()));
        }
        if (!schemaAnnotation.table().equals("")) {
            schemaBuilder.table(schemaAnnotation.table());
        } else if (tableUnderscore) {
            // 驼峰转下划线
            schemaBuilder.table(Strings.camelCaseToUnderscore(schemaBuilder.name()));
        }
        // clazz
        schemaBuilder.clazz(clazz);
        return schemaBuilder.build();
    }

    private void initColumns(SchemaInfo schema, Field[] fields) {
        for (Field field : fields) {
            ColumnInfo column = initColumn(field);
            if (column != null) {
                schema.addColumn(column);
            }
        }
    }

    private ColumnInfo initColumn(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation == null) {
            return null;
        }
        ColumnInfo.Builder columnBuilder = new ColumnInfo.Builder();
        // name
        columnBuilder.name(field.getName());
        // field
        if (!columnAnnotation.field().equals("")) {
            columnBuilder.field(columnAnnotation.field());
        } else if (fieldUnderscore) {
            // 驼峰装下划线
            columnBuilder.field(Strings.camelCaseToUnderscore(field.getName()));
        }
        // data type
        Class<?> type = field.getType();
        if (type == String.class) {
            columnBuilder.dataType(DataType.String);
        } else if (type == Integer.class) {
            columnBuilder.dataType(DataType.Int);
        } else if (type == Float.class) {
            columnBuilder.dataType(DataType.Float);
        } else if (type == Boolean.class) {
            columnBuilder.dataType(DataType.Bool);
        } else if (type == Long.class) {
            columnBuilder.dataType(DataType.Long);
        } else if (type == Date.class) {
            columnBuilder.dataType(DataType.Date);
        } else if (type.isEnum()) {
            columnBuilder.dataType(DataType.Enum);
        } else if (type == Double.class) {
            columnBuilder.dataType(DataType.Double);
        } else {
            try {
                throw new TypeNotSupportException(field);
            } catch (TypeNotSupportException e) {
                e.printStackTrace();
            }
        }
        // length
        if (columnAnnotation.length() > 0) {
            columnBuilder.length(columnAnnotation.length());
        }
        // primary key
        if (columnAnnotation.primaryKey()) {
            columnBuilder.isPrivateKey();
        }
        // generated key
        if (columnAnnotation.generatedKey()) {
            columnBuilder.isGeneratedKey();
        }
        return columnBuilder.build();
    }

    private void initAssociations(SchemaInfo schema, Field[] fields) {
        for (Field field : fields) {
            if (field.getAnnotation(com.github.orql.core.annotation.Column.class) != null) {
                continue;
            }
            BelongsTo belongsToAnnotation = field.getAnnotation(BelongsTo.class);
            if (belongsToAnnotation != null) {
                AssociationInfo.Builder builder = new AssociationInfo.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        AssociationInfo.Type.BelongsTo);
                // ref key
                if (! belongsToAnnotation.refKey().equals("")) {
                    builder.refKey(belongsToAnnotation.refKey());
                }
                // required
                builder.required(belongsToAnnotation.required());
                builder.build();
                continue;
            }
            HasOne hasOneAnnotation = field.getAnnotation(HasOne.class);
            if (hasOneAnnotation != null) {
                AssociationInfo.Builder builder = new AssociationInfo.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        AssociationInfo.Type.HasOne);
                // ref key
                if (! hasOneAnnotation.refKey().equals("")) {
                    builder.refKey(hasOneAnnotation.refKey());
                }
                // required
                builder.required(hasOneAnnotation.required());
                // cascade
                builder.onDelete(hasOneAnnotation.onDelete());
                builder.onUpdate(hasOneAnnotation.onUpdate());
                builder.build();
                continue;
            }
            HasMany hasManyAnnotation = field.getAnnotation(HasMany.class);
            if (hasManyAnnotation != null) {
                AssociationInfo.Builder builder = new AssociationInfo.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        AssociationInfo.Type.HasMany);
                // ref key
                if (! hasManyAnnotation.refKey().equals("")) {
                    builder.refKey(hasManyAnnotation.refKey());
                }
                // required
                builder.required(hasManyAnnotation.required());
                // cascade
                builder.onDelete(hasManyAnnotation.onDelete());
                builder.onUpdate(hasManyAnnotation.onUpdate());
                builder.build();
                continue;
            }
            BelongsToMany belongsToManyAnnotation = field.getAnnotation(BelongsToMany.class);
            if (belongsToManyAnnotation != null) {
                AssociationInfo.Builder builder = new AssociationInfo.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        AssociationInfo.Type.BelongsToMany);
                // middle
                Class<?> middleClass = belongsToManyAnnotation.middle();
                SchemaInfo middleSchema = getSchema(middleClass);
                builder.middleTable(middleSchema.getTable());
                // middle key
                if (! belongsToManyAnnotation.middleKey().equals("")) {
                    builder.middleKey(belongsToManyAnnotation.middleKey());
                }
                // ref middle key
                if (! belongsToManyAnnotation.refMiddleKey().equals("")) {
                    builder.refMiddleKey(belongsToManyAnnotation.refMiddleKey());
                }
                builder.build();
                continue;
            }
        }
    }

    private SchemaInfo getSchema(Field field) {
        if (field.getType() == List.class) {
            Type genericType = field.getGenericType();
            ParameterizedType pt = (ParameterizedType) genericType;
            // T class type
            Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
            return getSchema(Strings.toLowerCaseFirst(genericClazz.getSimpleName()));
        }
        return getSchema(Strings.toLowerCaseFirst(field.getType().getSimpleName()));
    }

    public Map<String, SchemaInfo> getSchemas() {
        return schemas;
    }
}
