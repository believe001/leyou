package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class GoodsController {
    @Autowired
    private SpuService spuService;

    /**
     * 根据key和上架与否分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "saleable", required = false) Boolean saleable,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "rows", defaultValue = "5") Integer rows
    ){
        PageResult<SpuBo> pageResult = this.spuService.querySpuBoByPage(key, saleable, page, rows);

        if(CollectionUtils.isEmpty(pageResult.getItems())){
            ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pageResult);

    }

    /**
     * 保存商品
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        this.spuService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据SpuId查询商品细节
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId") Long spuId){
        SpuDetail spuDetail= this.spuService.querySpuDetailBySpuId(spuId);
        if(spuDetail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(spuDetail);
    }
    /**
     * 根据SpuId查询SKU集合
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id") Long spuId){
        List<Sku> skus = this.spuService.querySkusBySpuId(spuId);
        if(CollectionUtils.isEmpty(skus)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(skus);
    }

    /**
     * 更新商品信息
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        try {
            this.spuService.updateGoods(spuBo);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }
}
