package utils;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//------------------------------------------------------------------------------
public final class Utils
{
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	public static final float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
	
	public void testEmail() {
	        Properties props = new Properties();
	        props.put("mail.smtp.auth", "true");
	        props.put("mail.smtp.port", "587");
	        props.put("mail.smtp.starttls.enable", "true");
	        String username = "jose", password = "yes";


	        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	            	logger.error("Authenticating....");
	                return new PasswordAuthentication(username, password);
	            }
	        });


	        try {
	            MimeMessage msg = new MimeMessage(session);
	            msg.setFrom(new InternetAddress(username));
	            msg.setRecipients(Message.RecipientType.TO, "jdischler.72@gmail.com");
	            msg.setSubject("Testing SMTP using ["+ username + "]");
	            msg.setSentDate(new Date());
	            msg.setText("Hey, this is a test from [" + username + "]");
	            Transport.send(msg);

	        } catch (MessagingException e) {
	            logger.error("send failed, exception: " + e);
	        }
	        logger.info("Sent Ok") ;
	}
	
	
}

