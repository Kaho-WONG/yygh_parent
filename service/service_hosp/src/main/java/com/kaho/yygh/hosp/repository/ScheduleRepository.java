package com.kaho.yygh.hosp.repository;

import com.kaho.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @description: 医院排班信息 MongoRepository 操作类
 * @author: Kaho
 * @create: 2023-02-25 20:16
 **/
@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {

    //根据医院编号 和 排班编号查询
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);
}
