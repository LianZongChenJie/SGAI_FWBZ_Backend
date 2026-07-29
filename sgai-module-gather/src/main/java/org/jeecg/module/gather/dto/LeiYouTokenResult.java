package org.jeecg.module.gather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeiYouTokenResult {

    /**
     * token
     */
    private String token;

    /**
     * tokenType
     */
    private String tokenType;

    /**
     * 过期时间
     */
    private String expiresIn;
}
