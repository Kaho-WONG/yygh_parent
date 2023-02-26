package com.kaho.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description: 数据字典服务远程调用 Feign 接口类
 * @author: Kaho
 * @create: 2023-02-26 20:06
 **/
@Component
@FeignClient("service-cmn") // 本接口所调用的目标微服务名称
public interface DictFeignClient {

    // 根据dictcode(上级编码)和value(值)查询数据字典名称
    @GetMapping("/admin/cmn/dict/getName/{dictCode}/{value}")
    public String getName(@PathVariable("dictCode") String dictCode, @PathVariable("value") String value);

    // 根据value(值)查询数据字典名称
    @GetMapping("/admin/cmn/dict/getName/{value}")
    public String getName(@PathVariable("value") String value);
}
