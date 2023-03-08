package com.bonuscarnisapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Sahkopostiviesti {
    //private SharedPreferences sharedPref;
    private Context context;
    private String viestinLahettaja;
    private String sahkopostinSalasana;
    private String viestinVastaanottaja;
    private String viestinAihe;
    private String viestinSisalto;
    // private viestinLiite todo;

    // Parametriton konstruktori
    Sahkopostiviesti(){};

    // Parametrillinen konstruktori
    public Sahkopostiviesti(Context context, String viestinAihe, String viestinSisalto){
        this.context = context;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Kehitystyön ja testauksen ajan:
        this.viestinLahettaja = "r13testi@gmail.com";
        this.sahkopostinSalasana = "chnommsbfyhdfxpm";
        this.viestinVastaanottaja = sharedPref.getString("defaultEmail", null);
        // Lopullisessa versiossa:
        //this.viestinLahettaja = sharedPref.getString("defaultEmail", null); // Haetaan asetuksista
        //this.sahkopostinSalasana = sharedPref.getString("defaultEmailPassword", null); // Haetaan asetuksista
        //this.viestinVastaanottaja = this.viestinLahettaja; // Oletuksena lähetetään samaan osoitteeseen
        // Viestin aihe, sisältö ja liitteet välitetään parametreina
        this.viestinAihe = viestinAihe;
        this.viestinSisalto = viestinSisalto;
        // Liitteet todo
    }

    // Metodi, joka lähettää sähköpostiviestin
    public void lahetaSahkopostiviesti(View view){
        try {
            String stringSenderEmail = this.viestinLahettaja; // lähettäjä
            String stringPasswordSenderEmail = this.sahkopostinSalasana; // 16-merkkinen GMAIL appPassword
            String stringReceiverEmail = this.viestinVastaanottaja; // vastaanottaja

            // Muut smtp-asetukset
            String stringHost = "smtp.gmail.com";
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            // Rakennetaan MimeMessage-viesti
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));
            mimeMessage.setSubject(this.viestinAihe); // Subject
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(this.viestinSisalto); // Body

            // Määritetään viestin liitteet
            //MimeBodyPart attachmentPart = new MimeBodyPart();
            //String filename = ""; // polku tiedostoon
            //DataSource source = new FileDataSource(filename);
            //attachmentPart.setDataHandler(new DataHandler(source));
            //attachmentPart.setFileName(filename);

            // Liitetään varsinainen viestiosa ja liiteosa yhteen
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            //multipart.addBodyPart(attachmentPart);
            mimeMessage.setContent(multipart);

            // Jotain määrityksiä...
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

            // Hoidetaan viestin lähetys uudessa säikeessä
            // HUOM. Pitäisikö käyttäjälle ilmoittaa onnistuiko/epäonnistuiko lähetys?
            // Alertit hankalia toteuttaa, sillä lähetys ja siten myös poikkeustenhallinta eri säikeessä kuin varsinainen graafinen käyttöliittymä
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
