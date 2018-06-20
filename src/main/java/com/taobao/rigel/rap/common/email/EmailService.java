package com.taobao.rigel.rap.common.email;

/**
 * 邮箱业务类
 */
public class EmailService {

    private String emailHost;
    private String emailPort;
    private String emailUser;
    private String emailPassword;
    private String fromEmail;


    /**
     * 发送HTML邮箱
     *
     * @return
     */
    public boolean sendHtmlEmail(String toEmail, String subject, String content) {

        //
        // 这个类主要是设置邮件
        //
        MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(emailHost);
        mailInfo.setMailServerPort(emailPort);
        mailInfo.setValidate(true);
        mailInfo.setUserName(emailUser);
        mailInfo.setPassword(emailPassword);// 您的邮箱密码

        mailInfo.setFromAddress(fromEmail);
        mailInfo.setToAddress(toEmail);

        mailInfo.setSubject(subject);
        mailInfo.setContent(content);

        // 这个类主要来发送邮件
        return SimpleMailSender.sendHtmlMail(mailInfo);// 发送文体格式
    }


    public void setEmailHost(String emailHost) {
        this.emailHost = emailHost;
    }

    public void setEmailPort(String emailPort) {
        this.emailPort = emailPort;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}
