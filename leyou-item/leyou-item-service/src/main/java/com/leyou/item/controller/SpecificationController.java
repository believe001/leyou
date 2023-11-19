package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;
    /**
     * 根据分类id查询分组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> specGroups = specificationService.queryGroupsByCid(cid);
        if(CollectionUtils.isEmpty(specGroups)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(specGroups);
    }

    /**
     * 根据分类id查询参数及组内参数组
     * @param cid
     * @return
     */
    @GetMapping("groups/params/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsWithParams(@PathVariable("cid") Long cid){
        List<SpecGroup> list = specificationService.queryGroupsWithParams(cid);
        if(CollectionUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }




    /**
     * 根据条件查询参数
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid", required = false)Long gid,
            @RequestParam(value = "cid", required = false)Long cid,
            @RequestParam(value = "generic", required = false)Boolean generic,
            @RequestParam(value = "searching", required = false)Boolean searching
            ){
        List<SpecParam> params = specificationService.queryParams(gid, cid, generic, searching);
        if(CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }
}
