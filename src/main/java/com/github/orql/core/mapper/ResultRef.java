package com.github.orql.core.mapper;

public class ResultRef extends Result {

    /**
     * 子对象
     */
    protected ResultRoot root;

    public ResultRoot getRoot() {
        return root;
    }

    public void setRoot(ResultRoot root) {
        this.root = root;
    }

}
