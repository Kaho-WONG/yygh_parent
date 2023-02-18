package com.kaho.yygh.cmn.controller;

import com.kaho.yygh.cmn.service.DictService;
import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@CrossOrigin // 处理跨域，等有nginx或网关后就不用这个注释了
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
    @GetMapping(value = "/exportData")
    public void exportDict(HttpServletResponse response) {
        dictService.exportDictData(response);
    }

}
