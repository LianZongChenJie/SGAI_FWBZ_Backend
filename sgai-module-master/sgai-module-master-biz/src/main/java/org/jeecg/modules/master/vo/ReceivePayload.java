package org.jeecg.modules.master.vo;

import lombok.Data;
import java.util.List;

@Data
public class ReceivePayload {
    private String source;
    private String systemCode;
    private IntegrationPayload.Type type;
    private IntegrationPayload.Op op;
    private String batchId;
    private List<DevicePushItem> data;
}
