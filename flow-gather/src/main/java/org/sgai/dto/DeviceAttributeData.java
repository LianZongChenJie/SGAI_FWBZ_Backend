package org.sgai.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeviceAttributeData {

    private String EquipmentCode;

    private String Timestamp;

    private List<AttributeData> Data;
}
