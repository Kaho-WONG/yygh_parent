package com.kaho.yygh.hosp.controller;

import com.kaho.yygh.hosp.service.HospitalSetService;
import com.kaho.yygh.model.hosp.HospitalSet;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: HospitalSetController 医院设置控制类，提供医院设置的相关方法
 * @author: Kaho
 * @create: 2023-02-14 22:16
 **/
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    //1.查询医院设置表(hospital_set)的所有数据，获取所有医院设置信息
    @GetMapping("findAll")
    public List<HospitalSet> findAllHospitalSet() {
        List<HospitalSet> list = hospitalSetService.list();
        return list;
    }

    //2.逻辑删除指定id的医院设置信息
    @DeleteMapping("{id}")
    public boolean removeHospSet(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        return flag;
    }
}
