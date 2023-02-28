package com.kaho.yygh.hosp.controller;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.hosp.service.HospitalService;
import com.kaho.yygh.model.hosp.Hospital;
import com.kaho.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @description: 医院列表 Controller
 * @author: Kaho
 * @create: 2023-02-26 16:11
 **/
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    // 展示医院列表(条件查询分页)
    @ApiOperation(value = "展示医院列表(条件查询分页)")
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable Integer page,
                           @PathVariable Integer limit,
                           HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> pageModel = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(pageModel);
    }

    // 更新医院上线状态
    @ApiOperation(value = "更新医院上线状态")
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable String id, @PathVariable Integer status) {
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    // 获取医院详情信息
    @ApiOperation(value = "获取医院详情信息")
    @GetMapping("showHospDetail/{id}")
    public Result showHospDetail(@PathVariable String id) {
        Map<String, Object> map = hospitalService.getHospById(id);
        return Result.ok(map);
    }

}
