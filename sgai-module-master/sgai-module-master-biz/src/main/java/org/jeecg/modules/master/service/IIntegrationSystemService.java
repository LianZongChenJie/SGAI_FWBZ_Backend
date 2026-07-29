package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.vo.IntegrationSystemForm;

public interface IIntegrationSystemService extends IService<IntegrationSystem> {

    void saveFromForm(IntegrationSystemForm form);

    void updateFromForm(IntegrationSystemForm form);

    IntegrationSystemForm getFormById(String id);

    IPage<IntegrationSystem> listPage(Page<IntegrationSystem> page, String name, String code);

    void removeByIdWithCheck(String id);

    /** 按 token 反查「启用接收」的系统（接收鉴权用）。 */
    IntegrationSystem findByToken(String token);
}
