package com.kaho.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.kaho.yygh.hosp.repository.HospitalRepository;
import com.kaho.yygh.hosp.service.HospitalService;
import com.kaho.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @description: 区别于 HospitalSetService，HospitalService类主要服务于外部的医院管理系统，
 * 外部的医院会调用我们(预约挂号)平台的ApiController控制类，ApiController提供的对外接口底层需要调用本类
 * HospitalServiceImpl提供的方法进行一些医院/科室/排班数据的存取，这些数据需要保存在平台的mongodb数据库中，
 * 后续显示到 预约挂号前台 供大众用户查看。
 *
 * @author: Kaho
 * @create: 2023-02-24 14:36
 **/
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository; // 注入，用来操作 mongodb

    @Override
    public void save(Map<String, Object> paramMap) {
        //把参数map集合转换为json字符串
        String mapString = JSONObject.toJSONString(paramMap);
        //再把json字符串转换为Hospital对象
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        //判断mongodb数据库中是否存在数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);

        //如果存在，进行修改
        if(hospitalExist != null) {
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else {
            //如果不存在，进行添加
            hospital.setStatus(0); //0：未上线 1：已上线
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }
}
