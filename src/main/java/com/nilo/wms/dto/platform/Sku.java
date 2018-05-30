package com.nilo.wms.dto.platform;

import com.nilo.wms.common.BaseDo;
import com.nilo.wms.dto.common.InterfaceConfig;

public class Sku extends BaseDo<Integer> {

    private String customerId;

    private String sku;

    private String desc_c;

    private String desc_e;

    private Integer length;

    private Integer width;

    private Integer height;

    private Integer price;

    private Integer weight;

    private String freightClass;

    private String image;

    private Integer status;

    private Integer cube;

    public Integer getCube() {
        return cube;
    }

    public void setCube(Integer cube) {
        this.cube = cube;
    }

    public String getFreightClass() {
        return freightClass;
    }

    public void setFreightClass(String freightClass) {
        this.freightClass = freightClass;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDesc_c() {
        return desc_c;
    }

    public void setDesc_c(String desc_c) {
        this.desc_c = desc_c;
    }

    public String getDesc_e() {
        return desc_e;
    }

    public void setDesc_e(String desc_e) {
        this.desc_e = desc_e;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }



    @Override
    public String toString() {
        return "Sku{" +
                "customerId='" + customerId + '\'' +
                ", sku='" + sku + '\'' +
                ", desc_c='" + desc_c + '\'' +
                ", desc_e='" + desc_e + '\'' +
                ", status=" + status +
                ", length=" + length +
                ", width=" + width +
                ", height=" + height +
                ", price=" + price +
                ", weight=" + weight +
                ", freightClass='" + freightClass + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
