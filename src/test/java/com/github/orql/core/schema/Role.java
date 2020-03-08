package com.github.orql.core.schema;

import com.github.orql.core.annotation.Column;
import com.github.orql.core.annotation.Schema;

@Schema
public class Role {

    @Column(primaryKey = true, generatedKey = true)
    private Integer id;

    @Column
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
