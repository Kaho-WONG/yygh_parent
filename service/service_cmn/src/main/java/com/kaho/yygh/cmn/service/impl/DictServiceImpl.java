package com.kaho.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaho.yygh.cmn.listener.DictListener;
import com.kaho.yygh.cmn.mapper.DictMapper;
import com.kaho.yygh.cmn.service.DictService;
import com.kaho.yygh.model.cmn.Dict;
import com.kaho.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 数据字典 service 实现类
 * @author: Kaho
 * @create: 2023-02-18 14:47
 **/
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Autowired
    private DictMapper dictMapper;

    // 根据数据id查询子数据列表
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator") // 按照RedisConfig中keyGenerator的配置生成key
    public List<Dict> findChlidData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        // 向list集合每个dict对象中设置hasChildren
        for (Dict dict : dictList) {
            Long dictId = dict.getId();
            boolean isChild = isChildren(dictId); // isChildren是提供来判断是否有子节点的
            dict.setHasChildren(isChild);
        }
        return dictList;
    }

    // 判断id下面是否有子节点
    private boolean isChildren(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        // 0>0无数据返回false    1>0有数据返回true
        return count > 0;
    }

    // 将数据字典导出为excel
    @Override
    public void exportDictData(HttpServletResponse response) {
        // 设置下载信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = "dict";
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        // 查询数据库
        List<Dict> dictList = baseMapper.selectList(null);
        // Dict -- DictEeVo
        List<DictEeVo> dictVoList = new ArrayList<>();
        for(Dict dict : dictList) {
            DictEeVo dictEeVo = new DictEeVo();
            //BeanUtils.copyProperties相当于执行了
            //dictEeVo.setId(dict.getId());
            //dictEeVo.setParentId(dict.getParentId());等封装了一系列操作
            BeanUtils.copyProperties(dict, dictEeVo);
            dictVoList.add(dictEeVo);
        }
        // 调用方法进行写操作
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict")
                    .doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 导入excel格式数据字典
    @Override
    @CacheEvict(value = "dict", allEntries = true) // allEntries = true: 方法调用后清空所有缓存
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet()
                    .doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 根据dictcode(上级编码)和value(值)查询数据字典名称
    @Override
    public String getDictName(String dictCode, String value) {
        // 如果dictCode为空，直接根据value查询
        if(StringUtils.isEmpty(dictCode)) {
            // 直接根据value查询
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value", value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        } else { // 如果dictCode不为空，根据dictCode和value查询
            // 根据dictcode查询dict对象，得到dict的id值
            Dict codeDict = this.getDictByDictCode(dictCode); // 拿到上级编码对应的父类别(省/医院等级/证件类型/学历...)
            Long parent_id = codeDict.getId(); // 取到这个类别的id，这个id就是我们下面需要查询的子数据字典项的parent_id
            // 根据parent_id和value进行查询
            Dict finalDict = baseMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id", parent_id)
                    .eq("value", value));
            return finalDict.getName();
        }
    }

    // 根据 dictCode 找到对应的父数据字典项(省/医院等级/证件类型/学历...)
    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        Dict codeDict = baseMapper.selectOne(wrapper);
        return codeDict;
    }

    // 根据dictCode获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        // 根据dictcode获取对应的父数据字典项，下面需要用到其id作为parentId查询其下级节点
        Dict dict = this.getDictByDictCode(dictCode);
        // 根据id获取子节点
        List<Dict> chlidData = this.findChlidData(dict.getId());
        return chlidData;
    }

}
