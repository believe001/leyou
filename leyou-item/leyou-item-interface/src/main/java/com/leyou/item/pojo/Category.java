package com.leyou.item.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
@Table(name = "tb_category")
public class Category  implements Serializable{
//    id         bigint(20)   (NULL)           NO      PRI     (NULL)   auto_increment  select,insert,update,references  类目id
//    name       varchar(32)  utf8_general_ci  NO              (NULL)                   select,insert,update,references  类目名称
//    parent_id  bigint(20)   (NULL)           NO      MUL     (NULL)                   select,insert,update,references  父类目id,顶级类目填0
//    is_parent  tinyint(1)   (NULL)           NO              (NULL)                   select,insert,update,references  是否为父节点，0为否，1为是
//    sort       int(4)       (NULL)           NO              (NULL)                   select,insert,update,references  排序指数，越小越靠前
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//主键自增长
    private Long id;
    private String name;
    private Long parentId;
    private Boolean isParent;
    private Integer sort;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Boolean getIsParent() {
        return isParent;
    }

    public void setIsParent(Boolean parent) {
        isParent = parent;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
