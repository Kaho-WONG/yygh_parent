package com.kaho.yygh.user.controller.api;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.common.utils.AuthContextHolder;
import com.kaho.yygh.model.user.Patient;
import com.kaho.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 就诊人管理 Api 接口
 * @author: Kaho
 * @create: 2023-03-04 17:11
 **/
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Autowired
    private PatientService patientService;

    //获取就诊人列表
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request) {
        //使用工具类AuthContextHolder获取当前登录用户的id
        Long userId = AuthContextHolder.getUserId(request);
        //根据当前用户id查找当前用户账户下的所有就诊人
        List<Patient> list = patientService.findAllByUserId(userId);
        return Result.ok(list);
    }

    //添加就诊人
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    //根据id(就诊人的id，非用户id)获取就诊人信息
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    //修改就诊人
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return Result.ok();
    }

    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public Result removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return Result.ok();
    }

    //根据就诊人id获取就诊人信息(用于被order模块远程调用，inner表示属于内部模块间调用的内部服务接口，不会被AuthGlobalFilter拦截)
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return patient;
    }

}
