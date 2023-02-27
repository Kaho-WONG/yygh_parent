package com.kaho.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.kaho.yygh.cmn.client.DictFeignClient;
import com.kaho.yygh.hosp.repository.HospitalRepository;
import com.kaho.yygh.hosp.service.HospitalService;
import com.kaho.yygh.model.hosp.Hospital;
import com.kaho.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
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

    @Autowired
    private DictFeignClient dictFeignClient; //用于远程调用service_cmn数据字典服务

    // 上传医院信息接口
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

    // 实现根据医院编号查询
    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    // 医院列表(条件查询分页)
    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        // 创建 pageable 对象
        Pageable pageable = PageRequest.of(page - 1, limit);
        // 创建条件匹配器(忽略大小写，模糊查询)
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        // hospitalQueryVo转换为hospital对象
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        // 创建对象
        Example<Hospital> example = Example.of(hospital, matcher);
        // 调用方法实现查询
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        // 获取查询到的list集合，遍历，把医院等级、医院地址封装到Hospital实体类的map集合
        // Hospital需要通过setHospitalHosType()方法里面的openfeign执行远程调用service_cmm模块获取
        pages.getContent().stream().forEach(item -> {
            this.setHospitalHosType(item);
        });

        return pages;
    }

    // 把医院等级、医院地址的真实名称封装到 hospital 实体中
    private Hospital setHospitalHosType(Hospital hospital) {
        // 根据dictCode(这里查医院，所以已知dictCode是Hostype)和value(对应等级的值)获取医院等级名称
        String hostypeString = dictFeignClient.getName("Hostype", hospital.getHostype());
        // 查询省 市 地区(省市地区的dictCode是空的，所以只传一个参数value)
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());

        // 下面getParam是继承于Hospital实体类的父类BaseMongoEntity的一个参数，用来保存额外信息(医院等级、医院地址的真实名称)
        hospital.getParam().put("fullAddress", provinceString+cityString+districtString);
        hospital.getParam().put("hostypeString", hostypeString);
        return hospital;
    }

    // 更新医院上线状态
    @Override
    public void updateStatus(String id, Integer status) {
        // 根据id从mongodb查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();
        // 设置修改的值
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital); // 保存进mongodb
    }

    // 查看医院详情信息
    @Override
    public Map<String, Object> getHospById(String id) {
        Map<String, Object> result = new HashMap<>();
        // 下面根据id查出mongodb中的医院实体后，需要对医院的一些信息做打包封装
        // 根据id查出医院，并把医院等级、医院地址的真实名称封装到 hospital 实体中
        Hospital hospital = this.setHospitalHosType(hospitalRepository.findById(id).get());
        // 医院基本信息（包含医院等级、医院地址【不是数据字典中的value】）
        result.put("hospital", hospital);
        // 单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        // 不需要重复返回
        hospital.setBookingRule(null);
        return result;
    }

    //根据hoscode获取医院名称
    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital != null) {
            return hospital.getHosname();
        }
        return null;
    }
}
