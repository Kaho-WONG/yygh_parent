package com.kaho.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.user.Patient;

import java.util.List;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-04 17:11
 **/
public interface PatientService extends IService<Patient> {

    //获取就诊人列表: 根据当前用户id查找当前用户账户下的所有就诊人
    List<Patient> findAllByUserId(Long userId);

    //根据id(就诊人的id，非用户id)获取就诊人信息
    Patient getPatientById(Long id);
}
