package com.github.orql.core.schema;

import com.github.orql.core.Cascade;

public class AssociationInfo {

    public enum Type {
        BelongsTo,
        HasOne,
        HasMany,
        BelongsToMany
    }

    private Type type;

    private String name;

    private SchemaInfo current;

    private SchemaInfo ref;

    private String refKey;

    private boolean required = true;

    private String middle;

    private String middleKey;

    private String refMiddleKey;

    private Cascade onUpdate;

    private Cascade onDelete;

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public SchemaInfo getRef() {
        return ref;
    }

    public ColumnInfo getRefId() {
        return ref.getIdColumn();
    }

    public String getRefKey() {
        return refKey;
    }

    public SchemaInfo getCurrent() {
        return current;
    }

    public ColumnInfo getCurrentId() {
        return current.getIdColumn();
    }

    public boolean isRequired() {
        return required;
    }

    public String getMiddle() {
        return middle;
    }

    public String getMiddleKey() {
        return middleKey;
    }

    public String getRefMiddleKey() {
        return refMiddleKey;
    }

    public Cascade getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(Cascade onUpdate) {
        this.onUpdate = onUpdate;
    }

    public Cascade getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(Cascade onDelete) {
        this.onDelete = onDelete;
    }

    public static class Builder {

        private AssociationInfo association;

        public Builder(String name, SchemaInfo current, SchemaInfo ref, Type type) {
            association = new AssociationInfo();
            association.name = name;
            association.ref = ref;
            association.current = current;
            association.type = type;
        }

        public Builder refKey(String refKey) {
            association.refKey = refKey;
            return this;
        }

        public Builder required(boolean required) {
            association.required = required;
            return this;
        }

        public Builder middle(String middle) {
            association.middle = middle;
            return this;
        }

        public Builder middleKey(String middleKey) {
            association.middleKey = middleKey;
            return this;
        }

        public Builder refMiddleKey(String refMiddleKey) {
            association.refMiddleKey = refMiddleKey;
            return this;
        }

        public Builder onUpdate(Cascade onUpdate) {
            association.onUpdate = onUpdate;
            return this;
        }

        public Builder onDelete(Cascade onDelete) {
            association.onDelete = onDelete;
            return this;
        }

        public void build() {
            switch (association.type) {
                case BelongsTo:
                    if (association.refKey == null) {
                        // user belongs to role
                        // refKey = roleId
                        association.refKey = association.ref.getName() + "Id";
                    }
                    break;
                case HasMany:
                    if (association.refKey == null) {
                        // user has many address
                        // refKey = userId
                        association.refKey = association.current.getName() + "Id";
                    }
                    break;
                case HasOne:
                    if (association.refKey == null) {
                        // user has one info
                        // refKey = userId
                        association.refKey = association.current.getName() + "Id";
                    }
                    break;
                case BelongsToMany:
                    break;
            }
            association.current.addAssociation(association);
        }

    }

}
