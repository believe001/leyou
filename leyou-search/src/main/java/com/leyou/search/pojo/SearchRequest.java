package com.leyou.search.pojo;

import java.util.Map;

public class SearchRequest {
    private String key; // 搜索条件
    private Integer page;// 当前的页数
    private static final Integer DEFAULT_SIZE = 20; // 每页条数
    private static final Integer DEFAULT_PAGE = 1; // 第几页
    private Map<String, String> filter; // 过滤条件

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if(page == null){
            return DEFAULT_PAGE;
        }
        // 获取页码时做一些校验，不能小于1
        return Math.max(DEFAULT_PAGE, page);
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public static Integer getDefaultSize() {
        return DEFAULT_SIZE;
    }

    public static Integer getDefaultPage() {
        return DEFAULT_PAGE;
    }
}
