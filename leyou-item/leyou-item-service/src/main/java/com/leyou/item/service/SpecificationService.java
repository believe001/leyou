package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据分类id查询分组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return specGroupMapper.select(record);
    }

    /**
     * 根据条件查询参数
     * @param gid
     * @return
     */
    public List<SpecParam> queryParams(Long gid,Long cid,Boolean generic,Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        return specParamMapper.select(specParam);

    }

    /**
     * 根据分类id查询参数及组内参数组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsWithParams(Long cid) {
        List<SpecGroup> specGroups = queryGroupsByCid(cid);
        for (SpecGroup specGroup : specGroups) {
            SpecParam specParam = new SpecParam();
            specParam.setGroupId(specGroup.getId());
            List<SpecParam> params = this.specParamMapper.select(specParam);
            specGroup.setParams(params);
        }

        return specGroups;
    }
}
