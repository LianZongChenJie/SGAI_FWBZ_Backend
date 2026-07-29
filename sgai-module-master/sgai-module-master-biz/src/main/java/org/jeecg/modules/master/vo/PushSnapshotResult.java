package org.jeecg.modules.master.vo;

import lombok.Data;

@Data
public class PushSnapshotResult {
    private String type;        // SPACE/CATEGORY/DEVICE
    private int payloadCount;
    private String status;      // SUCCESS/FAIL
    private String error;
}
