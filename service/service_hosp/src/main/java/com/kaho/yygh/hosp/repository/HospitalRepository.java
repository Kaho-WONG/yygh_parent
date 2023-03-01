package com.kaho.yygh.hosp.repository;

import com.kaho.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-02-24 14:30
 **/
@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {

    //判断是否存在数据
    //getHospitalByHoscode根据命名规范实现的方法(不用sql语句，有点mp的意思)：根据Hoscode的值获取Hospital对象，不需要我们自己去实现逻辑
    Hospital getHospitalByHoscode(String hoscode);

    //根据医院名称模糊查询医院
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
