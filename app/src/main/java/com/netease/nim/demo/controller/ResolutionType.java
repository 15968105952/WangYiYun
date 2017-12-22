package com.netease.nim.demo.controller;

/**
 * 分辨率类型
 * Created by hzxuwen on 2017/4/17.
 */

public enum ResolutionType {
    INVALID(-1),

    HD(0), // 高清

    SD(1), // 标清

    FLUENT(2); // 流畅


    private int value;

    ResolutionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ResolutionType getResolutionType(int value) {
        for (ResolutionType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }

        return INVALID;
    }
}
