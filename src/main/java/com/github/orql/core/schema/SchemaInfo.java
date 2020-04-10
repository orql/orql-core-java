package com.github.orql.core.schema;

import com.github.orql.core.Cascade;

import java.util.ArrayList;
import java.util.List;

public class SchemaInfo {

    /**
     * schema名称
     */
    private String name;

    /**
     * 数据库表名
     */
    private String table;

    /**
     * 类
     */
    private Class clazz;

    /**
     * id 列
     */
    private ColumnInfo idColumn;

    /**
     * 普通列list
     */
    private List<ColumnInfo> columns = new ArrayList<>();

    /**
     * 列名list
     */
    private List<String> columnNames = new ArrayList<>();

    /**
     * 关联对象list
     */
    private List<AssociationInfo> associations = new ArrayList<>();

    /**
     * 关联对象名list
     */
    private List<String> associationNames = new ArrayList<>();

    public String getName() {
        return name;
    }

    public ColumnInfo getIdColumn() {
        return idColumn;
    }

    public String getTable() {
        return table;
    }

    public Class getClazz() {
        return clazz;
    }

    public SchemaInfo addColumn(ColumnInfo column) {
        columnNames.add(column.getName());
        if (column.isPrivateKey()) {
            idColumn = column;
        }
        columns.add(column);
        return this;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public boolean containsColumn(String name) {
        return columnNames.contains(name);
    }

    public boolean containsAssociation(String name) {
        return associationNames.contains(name);
    }

    public ColumnInfo getColumn(String name) {
        int index = columnNames.indexOf(name);
        if (index >= 0) {
            return columns.get(index);
        }
        return null;
    }

    public AssociationInfo getAssociation(String name) {
        int index = associationNames.indexOf(name);
        if (index >= 0) {
            return associations.get(index);
        }
        return null;
    }

    public List<AssociationInfo> getAssociations() {
        return associations;
    }

    public AssociationInfo getAssociationByRefKey(String refKey) {
        for (AssociationInfo association : associations) {
            if (association.getRefKey().equals(refKey)) return association;
        }
        return null;
    }

    /**
     * 插入关联键
     * @param field
     * @param refSchema
     * @return
     */
    public SchemaInfo addRefColumn(String field, SchemaInfo refSchema) {
        return addRefColumn(field, refSchema, null, null);
    }

    public SchemaInfo addRefColumn(String field, SchemaInfo refSchema, Cascade onDelete, Cascade onUpdate) {
        for (ColumnInfo column : columns) {
            if (column.getName().equals(field)) {
                //重复不插入
                return this;
            }
        }
        ColumnInfo idColumn = refSchema.getIdColumn();
        ColumnInfo refColumn = new ColumnInfo.Builder()
                .name(field)
                .field(field)
                .isRefKey()
                .dataType(idColumn.getDataType()).isRefKey()
                .ref(refSchema)
                .onDelete(onDelete)
                .onUpdate(onUpdate)
                .build();
        addColumn(refColumn);
        return this;
    }

    public SchemaInfo addAssociation(AssociationInfo association) {
        associationNames.add(association.getName());
        this.associations.add(association);
        switch (association.getType()) {
            case HasOne:
            case HasMany:
                //插入外键到ref
                association.getRef().addRefColumn(
                        association.getRefKey(),
                        association.getRef(),
                        association.getOnDelete(),
                        association.getOnUpdate());
                break;
            case BelongsTo:
                // 插入外键到自己
                addRefColumn(association.getRefKey(), association.getRef());
                break;
        }
        return this;
    }

    public AssociationInfo.Builder belongsTo(String name, SchemaInfo ref) {
        return new AssociationInfo.Builder(name, this, ref, AssociationInfo.Type.BelongsTo);
    }

    public AssociationInfo.Builder hasOne(String name, SchemaInfo ref) {
        return new AssociationInfo.Builder(name, this, ref, AssociationInfo.Type.HasOne);
    }

    public AssociationInfo.Builder hasMany(String name, SchemaInfo ref) {
        return new AssociationInfo.Builder(name, this, ref, AssociationInfo.Type.HasMany);
    }

    public AssociationInfo.Builder belongsToMany(String name, SchemaInfo ref, String middleTable) {
        AssociationInfo.Builder builder = new AssociationInfo.Builder(name, this, ref, AssociationInfo.Type.HasMany);
        builder.middleTable(middleTable);
        return builder;
    }

    public ColumnInfo getColumn(int i) {
        return columns.get(i);
    }

    public String getIdName() {
        return idColumn.getName();
    }

    public String getIdField() {
        return idColumn.getField();
    }

    public static class Builder {

        private SchemaInfo schema = new SchemaInfo();

        public Builder name(String name) {
            schema.name = name;
            if (schema.table == null) {
                schema.table = name;
            }
            return this;
        }

        public String name() {
            return schema.name;
        }

        public Builder table(String table) {
            schema.table = table;
            return this;
        }

        public Builder clazz(Class clazz) {
            schema.clazz = clazz;
            return this;
        }

        public SchemaInfo build() {
            return schema;
        }
    }
}