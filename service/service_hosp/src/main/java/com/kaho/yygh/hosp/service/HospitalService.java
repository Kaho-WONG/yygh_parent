package com.kaho.yygh.hosp.service;

import com.kaho.yygh.model.hosp.Hospital;
import com.kaho.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @description:区别于 HospitalSetService，HospitalService类主要服务于外部的医院管理系统，
 * 外部的医院会调用我们(预约挂号)平台的ApiController控制类，ApiController提供的对外接口底层需要调用本类
 * HospitalServiceImpl提供的方法进行一些医院/科室/排班数据的存取，这些数据需要保存在平台的mongodb数据库中，
 * 后续显示到 预约挂号前台 供大众用户查看。
 *
 * @author: Kaho
 * @create: 2023-02-24 14:35
 **/
public interface HospitalService {

    // 上传医院信息接口
    void save(Map<String, Object> paramMap);

    // 实现根据医院编号查询
    Hospital getByHoscode(String hoscode);

    // 医院列表(条件查询分页)
    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    // 更新医院上线状态
    void updateStatus(String id, Integer status);

    // 医院详情信息
    Map<String, Object> getHospById(String id);

    //根据hoscode获取医院名称
    String getHospName(String hoscode);
}
