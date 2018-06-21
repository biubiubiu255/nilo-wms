package com.nilo.wms.service.scheduler;

import com.nilo.wms.common.util.MailInfo;
import com.nilo.wms.common.util.SendEmailUtil;
import com.nilo.wms.dao.platform.NotifyDao;
import com.nilo.wms.dto.platform.Notify;
import com.nilo.wms.service.platform.SpringContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 订单操作费
 * Created by Administrator on 2017/6/9.
 */
public class EmailWarningJob implements Job{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static AtomicBoolean RUN = new AtomicBoolean(false);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (RUN.compareAndSet(false, true)) {
                NotifyDao notifyDao = SpringContext.getBean(NotifyDao.class);
                List<Notify> failedList = notifyDao.queryFailed();
                if (failedList == null || failedList.size() == 0) return;
                //发送邮件
                sendFailedEmail(failedList);

            } else {
                logger.info("Already Execute EmailWarningJob.........");
            }
        } catch (Exception ex) {
            logger.error("EmailWarningJob failed. {}", ex.getMessage(), ex);
        } finally {
            RUN.set(false);
        }
    }

    private void sendFailedEmail(List<Notify> list) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setSubject("API Failed");
        List to = new ArrayList<>();
        to.add("ronny.zeng@kilimall.com");
        mailInfo.setToAddress(to);
        mailInfo.setContent(template(list));
        SendEmailUtil.sendEmail(mailInfo);
    }

    private String template(List<Notify> list) {
        String start = "<html> <head>   <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />   <meta name=\"renderer\" content=\"webkit\">   <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">   <link rel=\"stylesheet\" href=\"https://dms.kiliexpress.com/layui/css/layui.css\"  media=\"all\"> </head> <body> <table class=\"layui-table\">   <colgroup>     <col width=\"150\"> \t<col width=\"200\">     <col width=\"300\">     <col width=\"200\">     <col width=\"100\">     <col>   </colgroup>   <thead>     <tr>       <th>ID</th>       <th>URL</th>       <th>Param</th> \t  <th>Result</th> \t  <th>Num</th>     </tr>    </thead>   <tbody>";
        String end = "</table></body></html>";
        StringBuffer content = new StringBuffer();
        for (Notify n : list) {
            content.append("<tr><td>").append(n.getNotifyId()).append("</td><td>").append(n.getUrl()).append("</td><td>").append(n.getParam()).append("</td><td>").append(n.getResult() + "</td><td>" + n.getNum() + "</td></tr>");
        }
        return start + content.toString() + end;
    }
}
