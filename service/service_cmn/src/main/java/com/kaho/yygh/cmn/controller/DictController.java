package com.kaho.yygh.cmn.controller;

import com.kaho.yygh.cmn.service.DictService;
import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 数据字典控制类
 * @author: Kaho
 * @create: 2023-02-18 14:49
 **/
@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin // 处理跨域，等有nginx或网关后就不用这个注释了
public class DictController {

    @Autowired
    private DictService dictService;

    // 根据数据id查询子数据列表
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.findChlidData(id);
        return Result.ok(list);
    }

    // 将数据字典导出为excel
    @ApiOperation(value="导出excel")
    @GetMapping( "exportData")
    public void exportDict(HttpServletResponse response) {
        dictService.exportDictData(response);
    }

    // 导入excel格式数据字典
    @ApiOperation(value="导入excel")
    @PostMapping("importData")
    public Result importDict(MultipartFile file) {
        dictService.importDictData(file);
        return Result.ok();
    }

    //------------------------下面两个接口供service_hosp医院列表远程调用----------------------------
    // 根据dictcode(上级编码)和value(值)查询数据字典名称
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,
                          @PathVariable String value) {
        String dictName = dictService.getDictName(dictCode, value);
        return dictName;
    }

    // 根据value(值)查询数据字典名称
    @GetMapping("getName/{value}")
    public String getName(@PathVariable String value) {
        String dictName = dictService.getDictName("",value);
        return dictName;
    }
    //----------------------------------------------------------------------------------------

    // 根据dictCode获取下级节点
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }

}
