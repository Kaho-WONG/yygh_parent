package com.kaho.yygh.hosp.service;

import com.kaho.yygh.model.hosp.Schedule;
import com.kaho.yygh.vo.hosp.ScheduleOrderVo;
import com.kaho.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
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

    //根据医院编号 和 科室编号 ，查询排班规则数据
    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    //获取可预约的排班数据(指定医院指定科室下的)
    Map<String,Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode);

    //根据排班id获取排班数据
    Schedule getScheduleId(String scheduleId);

    //根据排班id获取预约下单信息(医院科室排班医生信息、剩余预约数、费用、时间等)
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //更新排班数据 mq监听器HospitalReceiver收到消息后调用此方法去mongodb中更新排班数据
    void update(Schedule schedule);
}
