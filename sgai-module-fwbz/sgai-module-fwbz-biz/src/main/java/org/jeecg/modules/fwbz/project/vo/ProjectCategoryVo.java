package org.jeecg.modules.fwbz.project.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCategoryVo {

    /**
     * 类别名称
     */
    private String categoryName;

    /**
     * 占比
     */
    private String value;
}
