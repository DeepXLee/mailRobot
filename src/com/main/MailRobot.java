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
 * ��ҳ��ز��Զ������ʼ�����,ֻ�ܷ��ͼ��ı�����,���ܷ���ͼƬ������
 * @author MAX
 *���������еĹؼ��ǵ���lib���е�mail.jar,��Ȼ�޷���������
 *�����������Ǽ����վ�������,����������κ��Զ������ʼ���ָ������
 */

public class MailRobot {

	//��־
	private static Logger logger = Logger.getLogger(MailRobot.class); 
	
	static String connectWebside1 = "";//��һ����վ��
	static String connectWebside2 = "";//�ڶ�����վ��
	static String mailAccount = "";//����������,ע��ֻ������,����@126.com
	static String mailPassword = "";//����������Ȩ��,ע���ǿͻ�����Ȩ��,��������,������������������
	static String mailRecipient = "";//�ռ��˵�ַ
	static String serviceAddress = "";//��������ַ,126�ĵ�ַ��:smtp.126.com

	static int count1 = 0 ;// ������
	static int count2 = 0 ;// ������

	// ������
	public static void main(String[] args) {
		
		try {
			// 1.ResourceBundleֻ�����ڶ�ȡproperties�ļ�,����ļ���ȡ����
			// 2.ResourceBundleֻ�����ڶ�ȡ,��������д��
			// 3.ResourceBundleֻ�ܶ�ȡ��·���µ�,������·���¶�ȡ����
			// ע��:����������д���ǰ��հ���.�����ķ�ʽд��,�벻Ҫд��չ��
			ResourceBundle resource = ResourceBundle.getBundle("config");
			//��ȡ�����ļ��еĲ���
			connectWebside1 = resource.getString("connectWebside1");
			connectWebside2 = resource.getString("connectWebside2");

			mailAccount = resource.getString("mailAccount");
			mailPassword = resource.getString("mailPassword");
			mailRecipient = resource.getString("mailRecipient");
			serviceAddress = resource.getString("serviceAddress");

			ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(2);// newһ����ʱ��

			// ��ʱ����������վ
			scheduled.scheduleAtFixedRate(new Runnable() {
				public void run() {
					try {
						System.out.println("�����վ1��������");
						HttpConnect.sendGet(connectWebside1);//������վ
						count1 = 0 ;//���ӳɹ������ü�����
					} catch (Exception e) {
						count1++;//����ʧ���������+1
						e.printStackTrace();
					}
					System.out.println("�����վ1ʧ�ܴ���:======" + count1);
					if (count1 == 5) {
						String webside = connectWebside1.substring(connectWebside1.indexOf("//") + 2,
								connectWebside1.indexOf("."));//��ȡ��վ�׵���,��Ϊ�ʼ��ķ�������
						sendMail(webside);//���÷����ʼ�����
						count1 = 0;//���ü�����
					}

				}
			}, 0, 2 * 60 * 1000, TimeUnit.MILLISECONDS);// 0��ʾ�״�ִ��������ӳ�ʱ�䣬�ڶ�����ʾÿ��ִ������ļ��ʱ�䣬TimeUnit.MILLISECONDSִ�е�ʱ������ֵ��λ

			// ��ʱ����������վ
			scheduled.scheduleAtFixedRate(new Runnable() {
				public void run() {
					try {
						System.out.println("�����վ2��������");
						HttpConnect.sendGet(connectWebside2);//������վ
						count2 = 0 ;//���ӳɹ������ü�����
					} catch (Exception e) {
						count2++;//����ʧ���������+1
						e.printStackTrace();
					}
					System.out.println("�����վ2ʧ�ܴ���:=====" + count2);
					if (count2 == 5) {
						String webside = connectWebside2.substring(connectWebside2.indexOf("//") + 2,
								connectWebside2.indexOf("."));//��ȡ��վ�׵���,��Ϊ�ʼ��ķ�������
						sendMail(webside);//���÷����ʼ�����
						count2 = 0;//���ü�����

					}

				}
			}, 0, 2 * 60 * 1000, TimeUnit.MILLISECONDS);// 0��ʾ�״�ִ��������ӳ�ʱ�䣬�ڶ�����ʾÿ��ִ������ļ��ʱ�䣬TimeUnit.MILLISECONDSִ�е�ʱ������ֵ��λ

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// �����ʼ��ķ���
	/* ������֤��Ϣ���ʼ� */
	private static boolean sendMail(String webside) {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", serviceAddress); // ���÷����ʼ����ʼ������������ԣ�����ʹ�����׵�smtp��������
		props.put("mail.smtp.host", serviceAddress); // ��Ҫ������Ȩ��Ҳ�����л����������У�飬��������ͨ����֤��һ��Ҫ����һ����
		props.put("mail.smtp.auth", "true"); // �øո����úõ�props���󹹽�һ��session
		Session session = Session.getDefaultInstance(props); // ������������ڷ����ʼ��Ĺ�������console����ʾ������Ϣ��������ʹ
																// �ã�������ڿ���̨��console)�Ͽ��������ʼ��Ĺ��̣�
		session.setDebug(true); // ��sessionΪ����������Ϣ����
		MimeMessage message = new MimeMessage(session); // ���ط����˵�ַ
		try {
			message.setFrom(
					new InternetAddress(mailAccount + "@" + serviceAddress.substring(serviceAddress.indexOf(".") + 1)));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailRecipient)); // �����ռ��˵�ַ
			message.setSubject(webside + "�����쳣"); // ���ر���
			Multipart multipart = new MimeMultipart(); // ��multipart����������ʼ��ĸ����������ݣ������ı����ݺ͸���
			BodyPart contentPart = new MimeBodyPart(); // �����ʼ����ı�����
			contentPart.setContent(webside + "�����쳣", "text/html;charset=utf-8");
			multipart.addBodyPart(contentPart);
			message.setContent(multipart);
			message.saveChanges(); // ����仯
			Transport transport = session.getTransport("smtp"); // ���ӷ�����������
			transport.connect(serviceAddress, mailAccount, mailPassword); // ��������
			transport.sendMessage(message, message.getAllRecipients());//�����ʼ�
			transport.close();
			logger.info(webside + "�����쳣,�ʼ��ѷ���");
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
