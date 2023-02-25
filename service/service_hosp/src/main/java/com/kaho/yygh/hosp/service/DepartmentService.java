package com.kaho.yygh.hosp.service;

import com.kaho.yygh.model.hosp.Department;
import com.kaho.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @description: 科室管理 service
 * @author: Kaho
 * @create: 2023-02-25 12:56
 **/
public interface DepartmentService {

    //上传科室信息
    void save(Map<String, Object> paramMap);

    //查询科室接口
    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    //删除科室接口
    void remove(String hoscode, String depcode);
}
