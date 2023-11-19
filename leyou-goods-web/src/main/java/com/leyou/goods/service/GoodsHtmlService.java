package com.leyou.goods.service;

import com.leyou.goods.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

// 用于保存静态的html
@Service
public class GoodsHtmlService {
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private GoodsService goodsService;

    /**
     * 创建html,保存在nginx html item做静态文件
     * @param spuId
     */
    public void createHtml(Long spuId){
        Map<String, Object> dataMap = goodsService.loadData(spuId);
        Context context = new Context();
        context.setVariables(dataMap);
        PrintWriter writer = null;
        try {
            File file = new File("D:\\APPS\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
            writer = new PrintWriter(file);
            // 执行静态化方法
            this.templateEngine.process("item",context,writer);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("页面静态化出错！！！");
        }finally {
            if(writer != null){
                writer.close();
            }
        }
        templateEngine.process("item", context, writer);
    }

    /**
     * 异步执行上面的操作，防止线程阻塞掉
     * @param spuId
     */
    public void asyncExecute(Long spuId){
        ThreadUtils.execute(()->createHtml(spuId));
    }
}
