package com.bugzillagmailtickets.bugupdater;

import javax.mail.Message;
import javax.mail.Multipart;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: saikat
 * Date: 10/07/13
 * Time: 8:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    private static boolean textIsHtml = false;
    public static void main(String[] args){
         System.out.println("Ticket generation process started.");
         Gmail gmail = new Gmail();
         Message[] messages = gmail.getMessages();
         Bugzilla bugzilla = new Bugzilla();
         Logger logger = Logger.getLogger("Main");
         for(Message message: messages){
             if(isNewBug(message)){
                 try{

                     String s = "";
                     Object msgContent = message.getContent();
                     if(msgContent instanceof String){
                         s = (String)msgContent;
                     }else if(msgContent instanceof Multipart){

                         Multipart mp = (Multipart)message.getContent();
                         for (int i = 0; i < mp.getCount(); i++) {
                             s = gmail.getText(mp.getBodyPart(i));
                         }
                     }

                 int bugId = bugzilla.createBug(message.getSubject(),s);
                 logger.info("bug Id "+bugId);

                 gmail.sendReply(bugId, message);

                 Message[] freshMessage = new Message[1];
                 freshMessage[0] = message;
                 gmail.setMessagesRead(freshMessage);

                 }catch (Exception e){
                     e.printStackTrace();
                     System.out.println("Unable to create bug");
                 }
             }else{
                 try{
                 String subj = message.getSubject();
                 System.out.println("Subject = "+subj);
                 int startIndex = subj.indexOf("[Bug")+5;
                 int endIndex = subj.indexOf("]");
                 String bugId = subj.substring(startIndex,endIndex);

                 String s = "";
                 Object msgContent = message.getContent();
                 if(msgContent instanceof String){
                     s = (String)msgContent;
                 }else if(msgContent instanceof Multipart){

                 Multipart mp = (Multipart)message.getContent();
                 for (int i = 0; i < mp.getCount(); i++) {
                      s = gmail.getText(mp.getBodyPart(i));
                 }
                 }
                 bugzilla.updateBugComment(bugId,s);

                 Message[] freshMessage = new Message[1];
                 freshMessage[0] = message;
                 gmail.setMessagesRead(freshMessage);

                 }catch(Exception e){
                     e.printStackTrace();
                     System.out.println("Unable to update bug");
                 }
             }
         }
        System.out.println("Ticket generation process ended.");
    }


    static boolean isNewBug(Message message){

        try{
            if(!(message.getSubject().contains("[Bug"))){
             return true;
            }
        }catch (Exception e){e.printStackTrace();}

        return false;
    }



}
