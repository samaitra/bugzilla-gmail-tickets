package com.bugzillagmailtickets.bugupdater;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;
import javax.mail.*;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

/**
 * Created with IntelliJ IDEA.
 * User: saikat
 * Date: 11/07/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class Gmail {

    private static boolean textIsHtml = false;

    String gmailId = "some_email@gmail.com";
    String password = "password";

    void sendReply(int bugId, Message msg){

        // Sender's email ID needs to be mentioned
        String from = gmailId;

        // Assuming you are sending email from localhost
        String host = "SMTP_MAIL_SERVER";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try{
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));



            Address[] address =  msg.getAllRecipients();
            InternetAddress[] toAddress = new InternetAddress[address.length];
            for(int i=0;i<address.length;i++){
                 System.out.println("Recipients Address = "+address[i].toString());
                 toAddress[i] = new InternetAddress(address[i].toString());
            }
            message.setRecipients(Message.RecipientType.TO,toAddress);

            // Set Subject: header field
            String mySubj = "[Bug " + bugId + "] " + msg.getSubject();
            System.out.println("mySubj = "+mySubj);
            message.setSubject(mySubj);

            String s = "";
            Multipart mp = (Multipart)msg.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                s = getText(mp.getBodyPart(i));
            }
            System.out.println("Reply content = "+s);
            message.setText(s);

            // Send message
            Transport.send(message);

            System.out.println("Message sent successfully....");
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }


    public Message[] getMessages() {
        Message[] messages = null;
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", gmailId, password);

            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            messages = inbox.search(ft);

            for (Message message : messages) {
                System.out.print(message.getMessageNumber()+" ");
                System.out.println(message.getSubject());
            }

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.out.println("No new messages found.");
            System.exit(1);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.exit(2);
        }

        return messages;
    }

    public void setMessagesRead(Message[] messages){
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", gmailId, password);

            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);
            inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.out.println("No new messages found.");
            System.exit(1);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.exit(2);
        }
        }

    /**
     * Return the primary text content of the message.
     */
    public static String getText(Part p) throws
            MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

}


