package com.main;

import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.*;

import javax.mail.internet.*;

import org.apache.log4j.Logger;

import com.http.HttpConnect;

/**
 * 网页监控并自动发送邮件程序,只能发送简单文本内容,不能发送图片附件等
 * @author MAX
 *本程序运行的关键是导入lib包中的mail.jar,不然无法正常运行
 *本程序作用是监控网站连接情况,连续错误五次后自动发送邮件到指定邮箱
 */

public class MailRobot {

	//日志
	private static Logger logger = Logger.getLogger(MailRobot.class); 
	
	static String connectWebside1 = "";//第一个网站名
	static String connectWebside2 = "";//第二个网站名
	static String mailAccount = "";//发件者名字,注意只是名字,不带@126.com
	static String mailPassword = "";//发件邮箱授权码,注意是客户端授权码,不是密码,在邮箱设置那里设置
	static String mailRecipient = "";//收件人地址
	static String serviceAddress = "";//服务器地址,126的地址是:smtp.126.com

	static int count1 = 0 ;// 计数用
	static int count2 = 0 ;// 计数用

	// 主程序
	public static void main(String[] args) {
		
		try {
			// 1.ResourceBundle只能用于读取properties文件,别的文件读取不了
			// 2.ResourceBundle只能用于读取,不能用于写入
			// 3.ResourceBundle只能读取类路径下的,不在类路径下读取不了
			// 注意:方法参数的写法是按照包名.类名的方式写的,请不要写扩展名
			ResourceBundle resource = ResourceBundle.getBundle("config");
			//读取配置文件中的参数
			connectWebside1 = resource.getString("connectWebside1");
			connectWebside2 = resource.getString("connectWebside2");

			mailAccount = resource.getString("mailAccount");
			mailPassword = resource.getString("mailPassword");
			mailRecipient = resource.getString("mailRecipient");
			serviceAddress = resource.getString("serviceAddress");

			ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(2);// new一个定时器

			// 定时运行连接网站
			scheduled.scheduleAtFixedRate(new Runnable() {
				public void run() {
					try {
						System.out.println("监控网站1正在运行");
						HttpConnect.sendGet(connectWebside1);//连接网站
						count1 = 0 ;//连接成功则重置计数器
					} catch (Exception e) {
						count1++;//连接失败则计数器+1
						e.printStackTrace();
					}
					System.out.println("监控网站1失败次数:======" + count1);
					if (count1 == 5) {
						String webside = connectWebside1.substring(connectWebside1.indexOf("//") + 2,
								connectWebside1.indexOf("."));//截取网站首单词,作为邮件的发送内容
						sendMail(webside);//调用发送邮件方法
						count1 = 0;//重置计数器
					}

				}
			}, 0, 2 * 60 * 1000, TimeUnit.MILLISECONDS);// 0表示首次执行任务的延迟时间，第二个表示每次执行任务的间隔时间，TimeUnit.MILLISECONDS执行的时间间隔数值单位

			// 定时运行连接网站
			scheduled.scheduleAtFixedRate(new Runnable() {
				public void run() {
					try {
						System.out.println("监控网站2正在运行");
						HttpConnect.sendGet(connectWebside2);//连接网站
						count2 = 0 ;//连接成功则重置计数器
					} catch (Exception e) {
						count2++;//连接失败则计数器+1
						e.printStackTrace();
					}
					System.out.println("监控网站2失败次数:=====" + count2);
					if (count2 == 5) {
						String webside = connectWebside2.substring(connectWebside2.indexOf("//") + 2,
								connectWebside2.indexOf("."));//截取网站首单词,作为邮件的发送内容
						sendMail(webside);//调用发送邮件方法
						count2 = 0;//重置计数器

					}

				}
			}, 0, 2 * 60 * 1000, TimeUnit.MILLISECONDS);// 0表示首次执行任务的延迟时间，第二个表示每次执行任务的间隔时间，TimeUnit.MILLISECONDS执行的时间间隔数值单位

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 发送邮件的方法
	/* 发送验证信息的邮件 */
	private static boolean sendMail(String webside) {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", serviceAddress); // 设置发送邮件的邮件服务器的属性（这里使用网易的smtp服务器）
		props.put("mail.smtp.host", serviceAddress); // 需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
		props.put("mail.smtp.auth", "true"); // 用刚刚设置好的props对象构建一个session
		Session session = Session.getDefaultInstance(props); // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
																// 用（你可以在控制台（console)上看到发送邮件的过程）
		session.setDebug(true); // 用session为参数定义消息对象
		MimeMessage message = new MimeMessage(session); // 加载发件人地址
		try {
			message.setFrom(
					new InternetAddress(mailAccount + "@" + serviceAddress.substring(serviceAddress.indexOf(".") + 1)));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailRecipient)); // 加载收件人地址
			message.setSubject(webside + "连接异常"); // 加载标题
			Multipart multipart = new MimeMultipart(); // 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
			BodyPart contentPart = new MimeBodyPart(); // 设置邮件的文本内容
			contentPart.setContent(webside + "连接异常", "text/html;charset=utf-8");
			multipart.addBodyPart(contentPart);
			message.setContent(multipart);
			message.saveChanges(); // 保存变化
			Transport transport = session.getTransport("smtp"); // 连接服务器的邮箱
			transport.connect(serviceAddress, mailAccount, mailPassword); // 连接邮箱
			transport.sendMessage(message, message.getAllRecipients());//发送邮件
			transport.close();
			logger.info(webside + "连接异常,邮件已发送");
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
