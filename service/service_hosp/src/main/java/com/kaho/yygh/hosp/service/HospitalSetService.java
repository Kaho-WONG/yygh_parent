package com.kaho.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.hosp.Hospital;
import com.kaho.yygh.model.hosp.HospitalSet;

/**
 * @description: HospitalSetService 医院设置 service 接口
 * @author: Kaho
 * @create: 2023-02-14 22:07
 **/
public interface HospitalSetService extends IService<HospitalSet> {

    /**
     * 获取签名key
     * @param hoscode
     * @return
     */
    String getSignKey(String hoscode);

}
