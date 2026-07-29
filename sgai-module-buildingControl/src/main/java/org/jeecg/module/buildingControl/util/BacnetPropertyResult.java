package org.jeecg.module.buildingControl.util;

/**
 * BACnet 属性读取结果
 */
public class BacnetPropertyResult {
    private String value;
    private String dataType;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
