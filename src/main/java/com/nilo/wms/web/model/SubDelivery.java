package com.nilo.wms.web.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017/4/18.
 */
@XmlRootElement(name = "header")
public class SubDelivery {

    String orderno;

    String relationorderNo;

    String boxcode;

    public String getOrderno() {
        return orderno;
    }

    @XmlElement(name = "OrderNo")
    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public String getRelationorderNo() {
        return relationorderNo;
    }

    public void setRelationorderNo(String relationorderNo) {
        this.relationorderNo = relationorderNo;
    }

    public String getBoxcode() {
        return boxcode;
    }

    public void setBoxcode(String boxcode) {
        this.boxcode = boxcode;
    }
}
