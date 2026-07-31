package org.jeecg.module.maintenance.dto;

import lombok.Data;

import java.util.List;

/**
 * 描述:
 *
 * @author ppliu
 * created in 2021/8/16 9:19
 */
@Data
public class TableHeader {
    private String field;
    private String name;
    private List<TableHeader> children;

    public TableHeader() {
    }

    public TableHeader(String name) {
        this.name = name;
    }

    public TableHeader(String field, String name) {
        this.field = field;
        this.name = name;
    }
}