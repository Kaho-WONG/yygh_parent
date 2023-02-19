package com.kaho.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.kaho.yygh.cmn.mapper.DictMapper;
import com.kaho.yygh.model.cmn.Dict;
import com.kaho.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

/**
 * @description: easyexcel 回调监听器
 * @author: Kaho
 * @create: 2023-02-19 13:57
 *
 * 有个很重要的点: DictListener不能被spring管理，每次读取excel都要new，所以里面用到spring的bean(dictMapper)时得用构造方法传进去
 **/
public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;
    /**
     * 使用了spring，要使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     * 即通过构造器注入
     * @param dictMapper
     */
    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    // 一行一行读取
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        // 将DictEeVo实体转为Dict实体
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        // 调用方法添加数据库
        dictMapper.insert(dict);
    }

    // 所有数据解析完成了 都会来调用
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
