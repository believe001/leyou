package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpuService {
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    /**
     * 根据key和上架与否查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        // 添加key查询关键字
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtil.isNotEmpty(key)){
            criteria.andLike("title", "%" + key + "%");
        }
        // 添加saleable查询关键字
        if(saleable != null){
            criteria.andEqualTo("saleable", saleable);
        }
        //分页:pageHelper后面必须立马跟着mapper查询才能分页
        PageHelper.startPage(page, rows);
        // 查询结果，Spu转换为SpuBo
        List<Spu> spus = this.spuMapper.selectByExample(example);
//        System.out.println("spus"+spus.toString());
        List<SpuBo> spuBos = spus.stream().map(
                spu -> {
                    SpuBo spuBo = new SpuBo();
                    BeanUtils.copyProperties(spu, spuBo);
                    Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
                    List<String> cnames = categoryService.queryCategoriesByIdList(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
                    String cname = StringUtils.join(cnames, "-");
                    spuBo.setCname(cname);
                    spuBo.setBname(brand.getName());
                    return spuBo;
                }
        ).collect(Collectors.toList());


        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
//        System.out.println("pageinfo"+pageInfo.toString());
        // 根据page和rows封装PageResult

        return new PageResult<SpuBo>(pageInfo.getTotal(), spuBos);


    }

    /**
     * 新增商品
     * @param spuBo
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        // 保存Spu
        // 设置一些默认字段的字段（前端没有传过来的字段）
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);
        // 保存Spudetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);
        saveSkusAndStocks(spuBo);
    }

    private void saveSkusAndStocks(SpuBo spuBo) {
        spuBo.getSkus().forEach(
                // 保存Sku
                sku -> {
                    sku.setSpuId(spuBo.getId());
                    sku.setEnable(true);
                    sku.setCreateTime(new Date());
                    sku.setLastUpdateTime(sku.getCreateTime());// 疑问？修改操作怎么直接获取的创建时间
                    // ？ 难道修改的同时一定会新增吗
                    this.skuMapper.insertSelective(sku);
                    // 保存Stock
                    Stock stock = new Stock();
                    stock.setSkuId(sku.getId());
                    stock.setStock(sku.getStock());
                    this.stockMapper.insertSelective(stock);
                }
        );
    }

    /**
     * 根据SpuId查询商品细节
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据SpuId查询SKU集合(注意还要查每个sku的库存)
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(sku);
        return skus.stream().map(s->{
            Stock stock = this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
            return s;
        }).collect(Collectors.toList());
    }

    /**
     * 更新商品信息
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        // 习惯上先删除子表再删除主表
        // 更新sku(先删除，再新增)
        // 删除sku和stock
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        List<Sku> oldSkus = this.skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(oldSkus)){
            oldSkus.forEach(s->{
                //更新stock(先删除，再新增)
                this.stockMapper.deleteByPrimaryKey(s.getId());
            });
        }
        // 删除以前的sku
        this.skuMapper.delete(sku);

        // 新增sku和stock
        this.saveSkusAndStocks(spuBo);
        //更新spu
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        //更新spu_detail
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

    }
}
