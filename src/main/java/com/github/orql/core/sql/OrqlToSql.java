package com.github.orql.core.sql;

import com.github.orql.core.*;
import com.github.orql.core.exception.SqlGenException;
import com.github.orql.core.orql.OrqlNode;
import com.github.orql.core.orql.OrqlNode.*;
import com.github.orql.core.schema.*;
import com.github.orql.core.sql.SqlNode.*;
import com.github.orql.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OrqlToSql {

    private Logger logger = LoggerFactory.getLogger(OrqlToSql.class);

    private SqlGenerator sqlGenerator = new SqlGenerator();

    private Map<OrqlRefItem, String> sqlCaches = new HashMap<>();

    public SqlGenerator getSqlGenerator() {
        return sqlGenerator;
    }

    /**
     * 查询包装类
     */
    private static class QueryWrapper {
        OrqlNode.OrqlRefItem item;
        String path;

        QueryWrapper(OrqlNode.OrqlRefItem item, String path) {
            this.item = item;
            this.path = path;
        }
    }

    public String toAdd(OrqlRefItem root) {
        if (sqlCaches.containsKey(root)) return sqlCaches.get(root);
        List<SqlColumn> columns = new ArrayList<>();
        List<SqlParam> params = new ArrayList<>();
        boolean selectAll = false;
        List<String> ignores = null;
        for (OrqlItem item : root.getChildren()) {
            if (item instanceof OrqlAllItem) {
                selectAll = true;
                ignores = new ArrayList<>();
            } else if (item instanceof OrqlIgnoreItem) {
                if (ignores == null) {
                    // 没有select all就忽略，抛出异常
                    throw new SqlGenException();
                }
                ignores.add(item.getName());
            } else if (item instanceof OrqlColumnItem) {
                ColumnInfo columnItem = ((OrqlColumnItem) item).getColumn();
                columns.add(new SqlColumn(columnItem.getField()));
                params.add(new SqlParam(columnItem.getName()));
            } else if (item instanceof OrqlRefItem) {
                AssociationInfo association = ((OrqlRefItem) item).getAssociation();
                if (association.getType() == AssociationInfo.Type.BelongsTo) {
                    columns.add(new SqlColumn(association.getRefKey()));
                    String param = association.getName() + "." + association.getRefId().getName();
                    params.add(new SqlParam(param));
                }
            }
        }
        if (selectAll) {
            for (ColumnInfo column : root.getRef().getColumns()) {
                if (column.isRefKey()) continue;
                if (ignores.contains(column.getName())) continue;
                columns.add(new SqlColumn(column.getField()));
                params.add(new SqlParam(column.getName()));
            }
        }
        SqlInsert insert = new SqlInsert(root.getRef().getTable(), columns, params);
        String sql = sqlGenerator.gen(insert);
        sqlCaches.put(root, sql);
        return sql;
    }

    public String toDelete(OrqlRefItem root) {
        if (sqlCaches.containsKey(root)) return sqlCaches.get(root);
        SqlExp exp = genExp(root.getWhere(), null);
        SqlDelete delete = new SqlDelete(root.getRef().getTable(), exp);
        String sql = sqlGenerator.gen(delete);
        sqlCaches.put(root, sql);
        return sql;
    }

    public String toUpdate(OrqlRefItem root) {
        if (sqlCaches.containsKey(root)) return sqlCaches.get(root);
        SqlExp exp = genExp(root.getWhere(), null);
        List<SqlUpdateColumn> columns = new ArrayList<>();
        boolean selectAll = false;
        List<String> ignores = null;
        for (OrqlItem item : root.getChildren()) {
            if (item instanceof OrqlAllItem) {
                selectAll = true;
                ignores = new ArrayList<>();
            } else if (item instanceof OrqlIgnoreItem) {
                // 没有选择全部忽略，抛出异常
                if (ignores == null) {
                    throw new SqlGenException();
                }
                ignores.add(item.getName());
            } else if (item instanceof OrqlColumnItem) {
                columns.add(new SqlUpdateColumn(((OrqlColumnItem) item).getColumn().getField(), ((OrqlColumnItem) item).getColumn().getName()));
            } else if (item instanceof OrqlRefItem) {
                AssociationInfo association = ((OrqlRefItem) item).getAssociation();
                switch (association.getType()) {
                    case BelongsTo:
                        // user belongsTo role
                        // roleId = $role.id
                        String param = association.getName() + "." + association.getRefId().getName();
                        columns.add(new SqlUpdateColumn(((OrqlRefItem) item).getAssociation().getRefKey(), param));
                        break;
                }
            }
        }
        if (selectAll) {
            for (ColumnInfo column : root.getRef().getColumns()) {
                if (column.isRefKey()) continue;
                if (ignores.contains(column.getName())) continue;
                columns.add(new SqlUpdateColumn(column.getField(), column.getName()));
            }
        }
        SqlUpdate update = new SqlUpdate(root.getRef().getTable(), exp, columns);
        String sql = sqlGenerator.gen(update);
        sqlCaches.put(root, sql);
        return sql;
    }

    public String toQuery(String op, OrqlRefItem root, boolean page, List<QueryOrder> orders) {
        SchemaInfo rootSchema = root.getRef();
        String table = rootSchema.getTable();
        List<SqlJoin> joins = new ArrayList<>();
        List<SqlExp> where = new ArrayList<>();
        //根节点exp
        SqlExp rootExp = null;
        List<SqlColumn> select = new ArrayList<>();
        // 排序
        List<SqlOrder> sqlOrders = new ArrayList<>();
        // 根节点排序
        List<SqlOrder> rootSqlOrders = new ArrayList<>();
        Stack<QueryWrapper> queryStack = new Stack<>();
        queryStack.push(new QueryWrapper(root, table));
        //存在数组类型关联
        boolean hasArrayRef = false;

        while (!queryStack.isEmpty()) {
            QueryWrapper queryWrapper = queryStack.pop();
            OrqlRefItem currentItem = queryWrapper.item;
            String columnPrefix = queryWrapper.path;
            SchemaInfo currentSchema = currentItem.getRef();
            ColumnInfo idColumn = currentSchema.getIdColumn();
            // 是否有主键
            boolean hasId = false;
            // 是否有select
            boolean hasSelect = false;
            // 选择全部
            boolean selectAll = false;
            // 忽略
            List<String> ignores = null;
            if (currentItem.getWhere() != null) {
                if (currentItem.getWhere() != null) {
                    SqlExp exp = genExp(currentItem.getWhere(), columnPrefix);
                    if (columnPrefix.equals(table)) {
                        // root where
                        rootExp = exp;
                    } else {
                        where.add(exp);
                    }
                }
            }
            for (OrqlItem child : currentItem.getChildren()) {
                hasSelect = true;
                if (child instanceof OrqlRefItem) {
                    AssociationInfo association = currentSchema.getAssociation(child.getName());

                    if (association.getType() == AssociationInfo.Type.HasMany || association.getType() == AssociationInfo.Type.BelongsToMany) {
                        if (!((OrqlRefItem) child).getChildren().isEmpty()) {
                            //存在数组类型关联
                            hasArrayRef = true;
                        }
                    }
                    SchemaInfo childSchema = ((OrqlRefItem) child).getRef();
                    ColumnInfo childIdColumn = childSchema.getIdColumn();
                    String childPath = columnPrefix + Constants.SqlSplit + child.getName();
                    //入栈
                    queryStack.push(new QueryWrapper((OrqlRefItem) child, childPath));
                    SqlJoinType joinType = association.isRequired() ? SqlJoinType.Inner : SqlJoinType.Left;
                    AssociationInfo.Type type = association.getType();
                    if (type == AssociationInfo.Type.HasMany) {
                        // role hasMany user
                        // user.roleId = role.id
                        SqlExp on = new SqlColumnExp(
                                new SqlColumn(association.getRefKey(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(childIdColumn.getField(), columnPrefix));
                        joins.add(new SqlJoin(childSchema.getTable(), childPath, joinType, on));
                    } else if (type == AssociationInfo.Type.HasOne) {
                        // user hasOne info
                        // info.userId = user.id
                        SqlExp on = new SqlColumnExp(
                                new SqlColumn(association.getRefKey(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(childIdColumn.getField(), columnPrefix));
                        joins.add(new SqlJoin(childSchema.getTable(), childPath, joinType, on));
                    } else if (type == AssociationInfo.Type.BelongsTo) {
                        // user belongsTo role
                        // role.id = user.roleId
                        SqlExp on = new SqlColumnExp(
                                new SqlColumn(association.getRefId().getField(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(association.getRefKey(), columnPrefix));
                        joins.add(new SqlJoin(childSchema.getTable(), childPath, joinType, on));
                    } else if (type == AssociationInfo.Type.BelongsToMany) {
                        // post belongsToMany tag, middle postTags
                        // postTags.postId = post.id
                        // postTags.tagId = tag.id
                        SchemaInfo targetSchema = association.getCurrent();
                        SchemaInfo foreign = association.getRef();
                        String middlePath = childPath + Constants.SqlSplit + association.getMiddle();
                        SqlExp leftOn = new SqlColumnExp(
                                new SqlColumn(association.getMiddleKey(), middlePath),
                                ExpOp.Eq,
                                new SqlColumn(childIdColumn.getField(), columnPrefix));
                        joins.add(new SqlJoin(association.getMiddle(), middlePath, joinType, leftOn));
                        SqlExp rightOn = new SqlColumnExp(
                                new SqlColumn(targetSchema.getIdColumn().getField(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(association.getRefMiddleKey(), middlePath));
                        joins.add(new SqlJoin(foreign.getTable(), childPath, joinType, rightOn));
                    }
                } else if (child instanceof OrqlNode.OrqlAllItem) {
                    selectAll = true;
                    ignores = new ArrayList<>();
                } else if (child instanceof OrqlIgnoreItem) {
                    // FIXME 可能会有空指针异常
                    ignores.add(child.getName());
                } else {
                    if (child.getName().equals(idColumn.getName())) {
                        hasId = true;
                    }
                    if (!op.equals("count")) {
                        if (child instanceof OrqlColumnItem) {
                            OrqlColumnItem columnItem = (OrqlColumnItem) child;
                            select.add(new SqlColumn(columnItem.getColumn().getField(), columnPrefix));
                        } else {
                            select.add(new SqlColumn(child.getName(), columnPrefix));
                        }
                    }
                }
            }
            if (selectAll) {
                for (ColumnInfo column : currentSchema.getColumns()) {
                    if (column.isRefKey()) continue;
                    if (ignores.contains(column.getName())) continue;
                    if (column.isPrivateKey()) hasId = true;
                    select.add(new SqlColumn(column.getField(), columnPrefix));
                }
            }
            if (!hasId) {
                if (!op.equals("count") && hasSelect) {
                    //插入id
                    select.add(new SqlColumn(idColumn.getField(), columnPrefix));
                }
            }
        }
        if (orders != null) {
            for (QueryOrder order : orders) {
                List<SqlColumn> columns = new ArrayList<>();
                for (String column : order.getColumns()) {
                    int index = column.lastIndexOf('.');
                    String currentPath = null;
                    String name = null;
                    if (index == -1) {
                        currentPath = rootSchema.getTable();
                        name = rootSchema.getColumn(column).getField();
                    } else {
                        currentPath = column.substring(0, index);
                        name = column.substring(index + 1);
                    }
                    // FIXME 多层column未支持
                    columns.add(new SqlColumn(name, currentPath));
                }
                SqlOrder sqlOrder = new SqlOrder(columns, order.getSort());
                sqlOrders.add(sqlOrder);
                if (Strings.countMatches(order.getColumns().get(0), ".") == 1) {
                    rootSqlOrders.add(sqlOrder);
                }
            }
        }
        //FIXME 逻辑太乱，后续修复
        SqlQuery query;
        if (op.equals("count")) {
            // 查询数量
            select.add(new SqlCountColumn(rootSchema.getIdField(), table));
            if (rootExp != null) where.add(0, rootExp);
            SqlForm from = new SqlTableForm(new SqlTable(table, table));
            query = new SqlQuery(select, from, where, joins, sqlOrders);
        } else if (hasArrayRef && page) {
            //嵌套分页查询
            List<SqlColumn> innerSelect = Collections.singletonList(new SqlColumn("*"));
            List<SqlExp> innerWhere = rootExp != null ? Collections.singletonList(rootExp) : new ArrayList<>();
            SqlTableForm innerFrom = new SqlTableForm(new SqlTable(table));
            SqlQuery innerQuery = new SqlQuery(innerSelect, innerFrom, innerWhere, new ArrayList<>(), rootSqlOrders);
            innerQuery.setPage(true);
            SqlForm from = new SqlInnerFrom(innerQuery);
            query = new SqlQuery(select, from, where, joins, sqlOrders);
            query.setPage(false);
        } else if (!hasArrayRef && !page && op.equals("queryOne")) {
            //没有设置分页，单个查询，而且没有数组类型关联查询
            // 自动添加limit 1分页优化
            if (rootExp != null) where.add(0, rootExp);
            SqlForm from = new SqlTableForm(new SqlTable(table, table));
            query = new SqlQuery(select, from, where, joins, sqlOrders);
            query.setLimit1(true);
        } else {
            if (rootExp != null) where.add(0, rootExp);
            SqlForm from = new SqlTableForm(new SqlTable(table, table));
            query = new SqlQuery(select, from, where, joins, sqlOrders);
            query.setPage(page);
        }
        return sqlGenerator.gen(query);
    }

    /**
     * 生成sql表达式
     * @param orqlExp
     * @param path 根节点为空
     * @return
     */
    private SqlExp genExp(OrqlExp orqlExp, String path) {
        if (orqlExp instanceof OrqlAndExp) {
            return new SqlAndExp(
                    genExp(((OrqlAndExp) orqlExp).getLeft(), path),
                    genExp(((OrqlAndExp) orqlExp).getRight(), path));
        }
        if (orqlExp instanceof OrqlOrExp) {
            return new SqlOrExp(
                    genExp(((OrqlOrExp) orqlExp).getLeft(), path),
                    genExp(((OrqlOrExp) orqlExp).getRight(), path));
        }
        if (orqlExp instanceof OrqlNestExp) {
            return new SqlNestExp(genExp(((OrqlNestExp) orqlExp).getExp(), path));
        }
        if (orqlExp instanceof OrqlColumnExp) {
            return genExpColumn((OrqlColumnExp) orqlExp, path);
        }
        throw new SqlGenException();
    }

    private SqlExp genExpColumn(OrqlColumnExp orqlColumnExp, String path) {
        SqlColumn left = new SqlColumn(orqlColumnExp.getLeft().getField(), path);
        if (orqlColumnExp.getRightColumn() != null) {
            SqlColumn right = new SqlColumn(orqlColumnExp.getRightColumn().getField(), path);
            return new SqlColumnExp(left, orqlColumnExp.getOp(), right);
        }
        if (orqlColumnExp.getRightParam() != null) {
            SqlParam right = new SqlParam(orqlColumnExp.getRightParam());
            return new SqlColumnExp(left, orqlColumnExp.getOp(), right);
        }
        Object right = orqlColumnExp.getRightValue();
        return new SqlColumnExp(left, orqlColumnExp.getOp(), right);
    }
}