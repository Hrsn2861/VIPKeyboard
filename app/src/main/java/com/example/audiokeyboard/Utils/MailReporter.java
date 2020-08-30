package com.example.audiokeyboard.Utils;

import android.content.Context;

import java.io.File;

public class MailReporter {
    private String mReceiveEmail;
    private String mSendEmail;
    private String mSendPassword;
    private String mHost;
    private String mPort;

    public MailReporter(Context context) {
    }

    /**
     * @param receiveEmail 设置接收者
     */
    public void setReceiver(String receiveEmail) {
        mReceiveEmail = receiveEmail;
    }

    /**
     * @param email 设置发送者邮箱
     */
    public void setSender(String email) {
        mSendEmail = email;
    }

    /**
     * @param password 设置发送者密码
     */
    public void setSendPassword(String password) {
        mSendPassword = password;
    }

    /**
     * @param host 设置SMTP 主机
     */
    public void setSMTPHost(String host) {
        mHost = host;
    }

    /**
     * @param port 设置端口
     */
    public void setPort(String port) {
        mPort = port;
    }

    public interface OnUploadFinishedListener {
        void onSuceess();


        void onError(String error);
    }

    public void sendEmailWithMultipleFiles(String title, String body, File[] files, OnUploadFinishedListener onUploadFinishedListener) {
        MailInfo sender = new MailInfo()
                .setUser(mSendEmail)
                .setPass(mSendPassword)
                .setFrom(mSendEmail)
                .setTo(mReceiveEmail)
                .setHost(mHost)
                .setPort(mPort)
                .setSubject(title)
                .setBody(body);
        sender.init();
        try {
            for (File file: files) {
                if (file.isFile()) {
                    sender.addAttachment(file.getPath(), file.getName());
                }
            }
            sender.send();
            onUploadFinishedListener.onSuceess();
        } catch (Exception e) {
            onUploadFinishedListener.onError("Send Email fail！Accout or SMTP verification error ！");
            e.printStackTrace();
        }
    }

    public void sendEmail(String title, String body, File file, OnUploadFinishedListener onUploadFinishedListener) {
        MailInfo sender = new MailInfo()
                .setUser(mSendEmail)
                .setPass(mSendPassword)
                .setFrom(mSendEmail)
                .setTo(mReceiveEmail)
                .setHost(mHost)
                .setPort(mPort)
                .setSubject(title)
                .setBody(body);
        sender.init();
        try {
            sender.addAttachment(file.getPath(), file.getName());
            sender.send();
            onUploadFinishedListener.onSuceess();
        } catch (Exception e) {
            onUploadFinishedListener.onError("Send Email fail！Accout or SMTP verification error ！");
            e.printStackTrace();
        }
    }
}
