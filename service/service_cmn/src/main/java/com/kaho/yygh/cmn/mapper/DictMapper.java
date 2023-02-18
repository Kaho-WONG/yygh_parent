package com.kaho.yygh.cmn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaho.yygh.model.cmn.Dict;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 数据字典 mapper
 * @author: Kaho
 * @create: 2023-02-18 14:40
 **/
@Mapper
public interface DictMapper extends BaseMapper<Dict> {
}
