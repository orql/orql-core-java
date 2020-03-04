package com.github.orql.core;

/**
 * 级联操作
 */
public enum Cascade {
    /**
     * 限制
     */
    Restrict,
    /**
     * 同步
     */
    NoAction,
    /**
     * 级联
     */
    Cascade,
    /**
     * 设null
     */
    SetNull
}
