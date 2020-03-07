package com.github.orql.core.orql;

import com.github.orql.core.ExpOp;
import com.github.orql.core.schema.AssociationInfo;
import com.github.orql.core.schema.ColumnInfo;
import com.github.orql.core.schema.SchemaInfo;

import java.util.List;

public class OrqlNode {

    private OrqlRefItem root;

    public OrqlNode(OrqlRefItem root) {
        this.root = root;
    }

    public OrqlRefItem getRoot() {
        return root;
    }

    public static class OrqlItem {

        private String name;

        public String getName() {
            return name;
        }

        public OrqlItem(String name) {
            this.name = name;
        }
    }

    public static class OrqlRefItem extends OrqlItem {

        private SchemaInfo ref;

        private AssociationInfo association;

        private OrqlExp where;

        private List<OrqlItem> children;

        public OrqlRefItem(String name, SchemaInfo ref, AssociationInfo association, List<OrqlItem> children, OrqlExp where) {
            super(name);
            this.ref = ref;
            this.association = association;
            this.children = children;
            this.where = where;
        }

        public SchemaInfo getRef() {
            return ref;
        }

        public AssociationInfo getAssociation() {
            return association;
        }

        public OrqlExp getWhere() {
            return where;
        }

        public List<OrqlItem> getChildren() {
            return children;
        }

        public Boolean isArray() {
            return association.getType() == AssociationInfo.Type.HasMany
                    || association.getType() == AssociationInfo.Type.BelongsToMany;
        }
    }

    public static class OrqlColumnItem extends OrqlItem {

        private ColumnInfo column;

        public OrqlColumnItem(ColumnInfo column) {
            super(column.getName());
            this.column = column;
        }

        public ColumnInfo getColumn() {
            return column;
        }

    }

    public static class OrqlIgnoreItem extends OrqlColumnItem {

        public OrqlIgnoreItem(ColumnInfo column) {
            super(column);
        }
    }

    public static class OrqlAllItem extends OrqlItem {

        public OrqlAllItem() {
            super("");
        }
    }

    public static class OrqlExp {

    }

    public static class OrqlNestExp extends OrqlExp {

        private OrqlExp exp;

        public OrqlNestExp(OrqlExp exp) {
            this.exp = exp;
        }

        public OrqlExp getExp() {
            return exp;
        }
    }

    public static class OrqlAndExp extends OrqlExp {

        private OrqlExp left;

        private OrqlExp right;

        public OrqlAndExp(OrqlExp left, OrqlExp right) {
            this.left = left;
            this.right = right;
        }

        public OrqlExp getLeft() {
            return left;
        }

        public OrqlExp getRight() {
            return right;
        }
    }

    public static class OrqlOrExp extends OrqlExp {

        private OrqlExp left;

        private OrqlExp right;

        public OrqlOrExp(OrqlExp left, OrqlExp right) {
            this.left = left;
            this.right = right;
        }

        public OrqlExp getLeft() {
            return left;
        }

        public OrqlExp getRight() {
            return right;
        }

    }

    public static class OrqlColumnExp extends OrqlExp {

        private ColumnInfo left;

        private ExpOp op;

        private ColumnInfo rightColumn;

        private String rightParam;

        private Object rightValue;

        public OrqlColumnExp(ColumnInfo left, ExpOp op, ColumnInfo right) {
            this.left = left;
            this.op = op;
            this.rightColumn = right;
        }

        public OrqlColumnExp(ColumnInfo left, ExpOp op, String right) {
            this.left = left;
            this.op = op;
            this.rightParam = right;
        }

        public OrqlColumnExp(ColumnInfo left, ExpOp op, Object right) {
            this.left = left;
            this.op = op;
            this.rightValue = right;
        }

        public ColumnInfo getLeft() {
            return left;
        }

        public ExpOp getOp() {
            return op;
        }

        public ColumnInfo getRightColumn() {
            return rightColumn;
        }

        public Object getRightValue() {
            return rightValue;
        }

        public String getRightParam() {
            return rightParam;
        }
    }

    public static class NullValue {

    }

}