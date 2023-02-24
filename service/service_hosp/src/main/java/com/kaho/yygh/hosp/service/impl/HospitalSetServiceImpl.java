package com.kaho.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaho.yygh.hosp.mapper.HospitalSetMapper;
import com.kaho.yygh.hosp.repository.HospitalRepository;
import com.kaho.yygh.hosp.service.HospitalSetService;
import com.kaho.yygh.model.hosp.Hospital;
import com.kaho.yygh.model.hosp.HospitalSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: HospitalSetServiceImpl 医院设置 service 接口实现类
 * @author: Kaho
 * @create: 2023-02-14 22:08
 **/
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {

    @Autowired
    private HospitalRepository hospitalRepository;

    // 根据传递过来医院编码，查询数据库，查询签名
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet.getSignKey();
    }

}
