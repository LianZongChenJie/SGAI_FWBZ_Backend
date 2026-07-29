package org.jeecg.modules.master.vo;

import lombok.Data;
import java.util.List;

@Data
public class IntegrationPayload {

    public enum Type { DEVICE, CATEGORY, SPACE }
    public enum Op { UPSERT, DELETE, SNAPSHOT }

    /** 固定 "sgai-master"（接收时为外部系统标识，由对方填） */
    private String source = "sgai-master";
    private String systemCode;
    private Type type;
    private Op op;
    private String batchId;
    /** 条目为 DevicePushItem / CategoryPushItem / SpacePushItem（按 type） */
    private List<Object> data;

    public int dataCount() {
        return data == null ? 0 : data.size();
    }
}
