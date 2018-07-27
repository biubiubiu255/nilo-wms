package com.nilo.wms.dto.inbound;

import com.nilo.wms.dto.outbound.ReportOutboundDetail;

/**
 * User: Alvin
 * Date: 2018/07/16 17:12
 * Just go for it and give it a try hhhh wait your beat
 */
public class InboundDetailParam extends ReportInboundDetail {
    private String fromDate;
    private String toDate;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
