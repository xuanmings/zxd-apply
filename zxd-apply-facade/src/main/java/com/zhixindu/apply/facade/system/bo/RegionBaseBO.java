package com.zhixindu.apply.facade.system.bo;

import java.io.Serializable;

/**
 * Created by SteveGuo on 2017/3/8.
 */
public class RegionBaseBO implements Serializable {

    private static final long serialVersionUID = -5055181672798579134L;

    /** 区划代码 */
    private Integer code;
    /** 行政区名称（不含单位） */
    private String name;
    /** 全名 */
    private String full_name;
    /** 区划级别（1省，2市，3区/县） */
    private Integer region_level;
    /** 上一级代码 */
    private Integer parent_code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public Integer getRegion_level() {
        return region_level;
    }

    public void setRegion_level(Integer region_level) {
        this.region_level = region_level;
    }

    public Integer getParent_code() {
        return parent_code;
    }

    public void setParent_code(Integer parent_code) {
        this.parent_code = parent_code;
    }
}
