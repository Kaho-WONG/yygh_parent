package com.kaho.yygh.hosp.service;

import com.kaho.yygh.model.hosp.Schedule;
import com.kaho.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @description: 排班管理 service
 * @author: Kaho
 * @create: 2023-02-25 20:19
 **/
public interface ScheduleService {

    //上传排班接口
    void save(Map<String, Object> paramMap);

    //查询排班接口
    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    //删除排班接口
    void remove(String hoscode, String hosScheduleId);
}
