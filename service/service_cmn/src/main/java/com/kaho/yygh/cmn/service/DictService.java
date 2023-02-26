package com.kaho.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: DictService 数据字典 service 接口
 * @author: Kaho
 * @create: 2023-02-18 14:45
 **/
public interface DictService extends IService<Dict> {

    // 根据数据id查询子数据列表
    List<Dict> findChlidData(Long id);

    // 将数据字典导出为excel
    void exportDictData(HttpServletResponse response);

    // 导入excel格式数据字典
    void importDictData(MultipartFile file);

    // 根据dictcode(上级编码，可能为空)和value(值)查询
    String getDictName(String dictCode, String value);

    // 根据dictCode获取下级节点
    List<Dict> findByDictCode(String dictCode);
}
