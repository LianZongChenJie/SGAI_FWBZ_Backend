package org.jeecg.modules.master.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ReceiveResult {
    private String batchId;
    private int accepted;
    private final List<Reject> rejected = new ArrayList<>();

    @Data
    public static class Reject {
        private final String id;
        private final String reason;
    }
}
