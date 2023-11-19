package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("spec")
public interface SpecificationApi {
    /**
     * 根据条件查询参数
     *
     * @param gid
     * @return
     */
    @GetMapping("params")
    public List<SpecParam> queryParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching", required = false) Boolean searching
    );

    /**
     * 根据分类id查询分组
     *
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public List<SpecGroup> queryGroupsByCid(@PathVariable("cid") Long cid);

    /**
     * 根据分类id查询参数及组内参数组
     * @param cid
     * @return
     */
    @GetMapping("groups/params/{cid}")
    public List<SpecGroup> queryGroupsWithParams(@PathVariable("cid") Long cid);
}
