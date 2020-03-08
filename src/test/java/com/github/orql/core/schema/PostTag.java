package com.github.orql.core.schema;

import com.github.orql.core.annotation.Schema;
import com.github.orql.core.annotation.BelongsTo;

@Schema
public class PostTag {

    @BelongsTo
    private Post post;

    @BelongsTo
    private Tag tag;

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
