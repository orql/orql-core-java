package com.github.orql.core.schema;

import com.github.orql.core.annotation.BelongsTo;
import com.github.orql.core.annotation.Column;
import com.github.orql.core.annotation.Schema;

@Schema(table = "user_info")
public class UserInfo {

    @Column(primaryKey = true, generatedKey = true)
    private Long id;

    @Column
    private String birthday;

    @BelongsTo
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
