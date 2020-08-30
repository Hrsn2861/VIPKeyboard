package com.example.audiokeyboard.Utils;

import com.czm.library.save.imp.LogWriter;
import com.elvishew.xlog.XLog;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class Logger {
    public static class Builder {
        private String tag = "";
        public Builder() {}
        public Builder tag(String t) {
            this.tag = t;
            return this;
        }
        public void d(String format, Object... args) {
            build().d(format, args);
        }
        public void d(String msg) {
            build().d(msg);
        }
        public void i(String format, Object... args) {
            build().i(format, args);
        }
        public void i(String msg) {
            build().i(msg);
        }
        public Logger build() {
            return new Logger(this);
        }
    }
    public static String defaultTag = "";
    public static boolean outputToConsole = false;
    private Logger() {}
    private Logger(Builder b) {
        defaultTag = b.tag;
    }
    public static void setTag(String tag) {
        defaultTag = tag;
    }

    public static void d(String msg) {
        long timestamp = System.currentTimeMillis();
        log(timestamp, "D", defaultTag, msg);
    }

    public static void d(String format, Object... args) {
        String msg = String.format(format, args);
        d(msg);
    }


    public static void i(String msg) {
        long timestamp = System.currentTimeMillis();
        log(timestamp, "I", defaultTag, msg);
    }

    public static void i(String format, Object... args) {
        String msg = String.format(format, args);
        i(msg);
    }


    public static void log(long timestamp, String level, String tag, String msg) {
        String s = String.format("%d|%s", timestamp, msg);
        if (level.equals("I")) {
            XLog.tag(tag).i(s);
        } else if (level.equals("D")) {
            XLog.tag(tag).d(s);
        }
        if (outputToConsole) {
            System.out.println(level + "|" + s);
        }
        LogWriter.writeLog(level, s);
    }

    public static Logger.Builder tag(String tag) {
        return new Logger.Builder().tag(tag);
    }



   /*public static boolean sendEmail(String fileName) {
        Properties props = new Properties();
        props.put("mail.smtp.protocol", "smtp");
        props.put("mail.smtp.auth", "true");//设置要验证
        props.put("mail.smtp.host", "smtp.126.com");//设置host
        props.put("mail.smtp.port", "25");  //设置端口
        PassAuthenticator pass = new PassAuthenticator();   //获取帐号密码
        Session session = Session.getInstance(props, pass); //获取验证会话
        try
        {
            //配置发送及接收邮箱
            InternetAddress fromAddress, toAddress;
            fromAddress = new InternetAddress("swnhieian@126.com", "自己给自己发");
            toAddress   = new InternetAddress("swnhieian@126.com", "自己接收自己发的邮件");

            MimeBodyPart attachPart = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(fileName); //打开要发送的文件

            attachPart.setDataHandler(new DataHandler(fds));
            attachPart.setFileName(fds.getName());
            MimeMultipart allMultipart = new MimeMultipart("mixed"); //附件
            allMultipart.addBodyPart(attachPart);//添加
            //配置发送信息
            MimeMessage message = new MimeMessage(session);
//                message.setContent("test", "text/plain");
            message.setContent(allMultipart); //发邮件时添加附件
            message.setSubject("[log] 测试");
            message.setFrom(fromAddress);
            message.addRecipient(javax.mail.Message.RecipientType.TO, toAddress);
            message.saveChanges();
            //连接邮箱并发送
            Transport transport = session.getTransport("smtp");

            transport.connect("smtp.126.com", "swnhieian@126.com", "IYTYJGRJOWQAVOCH");
            transport.send(message);
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();//将此异常向上抛出，此时CrashHandler就能够接收这里抛出的异常并最终将其存放到txt文件中
               Log.e("sendmail", e.getMessage());
        }
        return true;
    }*/

}
