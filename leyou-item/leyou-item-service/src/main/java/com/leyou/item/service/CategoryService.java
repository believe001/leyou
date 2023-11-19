package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 根据父ID查询子节点
 */
@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据parentId查询种类集合
     * @param pid
     * @return
     */
    public List<Category> queryCategoriesByPid(Long pid){
        Category record = new Category();
        record.setParentId(pid);
        return categoryMapper.select(record);
    }

    /**
     * 根据idList查询种类名称
     * @param idList
     * @return
     */
    public List<String> queryNamesByIdList(List<Long> idList){
        List<Category> categories = categoryMapper.selectByIdList(idList);
        return categories.stream().map(
                ct-> ct.getName()
        ).collect(Collectors.toList());
    }


}
