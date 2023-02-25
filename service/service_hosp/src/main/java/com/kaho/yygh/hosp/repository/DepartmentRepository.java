package com.kaho.yygh.hosp.repository;

import com.kaho.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @description: 医院科室 操作 MongoRepository
 * @author: Kaho
 * @create: 2023-02-25 12:47
 **/
@Repository
public interface DepartmentRepository extends MongoRepository<Department, String>  {

    //通过医院编号和科室编号获取对应科室对象
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
