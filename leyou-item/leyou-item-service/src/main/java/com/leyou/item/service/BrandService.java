package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;
@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据条件分页查询并排序品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc){
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        // 根据name模糊查询，或者根据首字母查询
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }
        //添加分页条件
        PageHelper.startPage(page, rows);

        // 添加排序条件
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }
        // 包装成pageInfo
        List<Brand> brands = brandMapper.selectByExample(example);
        PageInfo<Brand> brandPageInfo = new PageInfo<>(brands);
        // 包装成分页结果集返回
        return new PageResult<Brand>(brandPageInfo.getTotal(), brandPageInfo.getList());
    }

    /**
     * 保存品牌信息
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
//        System.out.println("品牌"+brand);
//        System.out.println("种类:"+cids);
        // 新增brand
//        System.out.println(brand);
        this.brandMapper.insertSelective(brand);
//        System.out.println(brand);//上面一句执行后，主键会自动赋值

        // 新增中间表
        for (Long cid : cids) {
            System.out.println("存储对cid,bid" + cid + ":" + brand.getId());
            this.brandMapper.insertBrandAndCategory(cid, brand.getId());
        }
    }
    /**
     * 根据分类id查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryBrandsByCid(Long cid) {
        return this.brandMapper.queryBrandsByCid(cid);
    }
}
