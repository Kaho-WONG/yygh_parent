package com.kaho.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.cmn.Dict;

import java.util.List;

/**
 * @description: DictService 数据字典 service 接口
 * @author: Kaho
 * @create: 2023-02-18 14:45
 **/
public interface DictService extends IService<Dict> {

    // 根据数据id查询子数据列表
    List<Dict> findChlidData(Long id);

}
