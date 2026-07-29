package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.master.entity.Space;

import java.util.List;

public interface ISpaceService extends IService<Space> {
    List<Space> listAll(String name);
    void create(Space entity);
    void updateNode(Space entity);
    void removeNode(String id);
}
