package com.kaho.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.common.util.MD5;
import com.kaho.yygh.hosp.service.HospitalSetService;
import com.kaho.yygh.model.hosp.HospitalSet;
import com.kaho.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * @description: HospitalSetController 医院设置控制类，提供医院设置的相关方法
 * @author: Kaho
 * @create: 2023-02-14 22:16
 **/
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    //1.查询医院设置表(hospital_set)的所有数据，获取所有医院设置信息
    @ApiOperation(value = "获取所有医院设置信息")
    @GetMapping("findAll")
    public Result findAllHospitalSet() {
        //调用service的方法
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    //2.逻辑删除指定id的医院设置信息
    @ApiOperation(value = "逻辑删除指定id的医院设置信息")
    @DeleteMapping("{id}")
    public Result removeHospSet(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //3.条件查询带分页
    @ApiOperation(value = "条件查询医院设置信息分页显示")
    @PostMapping("findPageHospSet/{current}/{limit}")
    public Result findPageHospSet(@ApiParam("当前页") @PathVariable long current,
                                  @ApiParam("每页记录条数") @PathVariable long limit,
                                  @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        // 创建page对象，传递当前页，每页记录数
        Page<HospitalSet> page = new Page<>(current,limit);
        // 构建条件
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        String hosname = hospitalSetQueryVo.getHosname(); // 医院名称
        String hoscode = hospitalSetQueryVo.getHoscode(); // 医院编号
        if(!StringUtils.isEmpty(hosname)) {
            wrapper.like("hosname", hosname);
        }
        if(!StringUtils.isEmpty(hoscode)) {
            wrapper.eq("hoscode", hoscode);
        }
        // 调用方法实现分页查询
        IPage<HospitalSet> pageHospitalSet = hospitalSetService.page(page, wrapper);
        // 返回结果
        return Result.ok(pageHospitalSet);
    }

    //4.添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet) {
        // 设置状态: 1 使用 0 不能使用
        hospitalSet.setStatus(1);
        // 签名秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        // 调用service进行数据插入
        boolean save = hospitalSetService.save(hospitalSet);
        if(save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //5.根据id获取医院设置信息
    @ApiOperation(value = "根据id获取医院设置信息")
    @GetMapping("getHospSet/{id}")
    public Result getHospSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    //6.修改医院设置信息
    @ApiOperation(value = "修改医院设置信息")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //7.批量删除医院设置
    @ApiOperation(value = "批量删除医院设置信息")
    @DeleteMapping("batchRemove")
    public Result batchRemoveHospitalSet(@RequestBody List<Long> idList) {
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    //8.医院设置锁定和解锁
    @ApiOperation(value = "医院设置锁定与解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,
                                  @PathVariable Integer status) {
        // 根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        // 设置状态: 1表示解锁，0表示不可用
        hospitalSet.setStatus(status);
        // 调用方法
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }

    //9.发送签名密钥(通过短信形式发送给医院联系人，医院联系人拿到密钥就能将医院系统与本预约挂号管理系统对接)
    @ApiOperation(value = "发送签名密钥")
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey(); // 签名密钥
        String hoscode = hospitalSet.getHoscode(); // 医院编号
        //TODO 发送短信
        return Result.ok();
    }

}
