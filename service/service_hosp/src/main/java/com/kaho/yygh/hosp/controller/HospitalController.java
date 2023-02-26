package com.kaho.yygh.hosp.controller;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.hosp.service.HospitalService;
import com.kaho.yygh.model.hosp.Hospital;
import com.kaho.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 医院列表 Controller
 * @author: Kaho
 * @create: 2023-02-26 16:11
 **/
@RestController
@RequestMapping("/admin/hosp/hospital")
@CrossOrigin
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    // 展示医院列表(条件查询分页)
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable Integer page,
                           @PathVariable Integer limit,
                           HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> pageModel = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(pageModel);
    }

}
