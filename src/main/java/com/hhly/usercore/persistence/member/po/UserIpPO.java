package com.hhly.usercore.persistence.member.po;

/**
 * 
 * @desc    
 * @author  cheng chen
 * @date    2018年4月25日
 * @company 益彩网络科技公司
 * @version 1.0
 */
public class UserIpPO {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private Long startipnum;

    /**
     * 
     */
    private String startiptext;

    /**
     * 
     */
    private Long endipnum;

    /**
     * 
     */
    private String endiptext;

    /**
     * 
     */
    private String country;

    /**
     * 
     */
    private String local;

    /**
     * 
     * @return id 
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id 
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return startipnum 
     */
    public Long getStartipnum() {
        return startipnum;
    }

    /**
     * 
     * @param startipnum 
     */
    public void setStartipnum(Long startipnum) {
        this.startipnum = startipnum;
    }

    /**
     * 
     * @return startiptext 
     */
    public String getStartiptext() {
        return startiptext;
    }

    /**
     * 
     * @param startiptext 
     */
    public void setStartiptext(String startiptext) {
        this.startiptext = startiptext == null ? null : startiptext.trim();
    }

    /**
     * 
     * @return endipnum 
     */
    public Long getEndipnum() {
        return endipnum;
    }

    /**
     * 
     * @param endipnum 
     */
    public void setEndipnum(Long endipnum) {
        this.endipnum = endipnum;
    }

    /**
     * 
     * @return endiptext 
     */
    public String getEndiptext() {
        return endiptext;
    }

    /**
     * 
     * @param endiptext 
     */
    public void setEndiptext(String endiptext) {
        this.endiptext = endiptext == null ? null : endiptext.trim();
    }

    /**
     * 
     * @return country 
     */
    public String getCountry() {
        return country;
    }

    /**
     * 
     * @param country 
     */
    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    /**
     * 
     * @return local 
     */
    public String getLocal() {
        return local;
    }

    /**
     * 
     * @param local 
     */
    public void setLocal(String local) {
        this.local = local == null ? null : local.trim();
    }
}