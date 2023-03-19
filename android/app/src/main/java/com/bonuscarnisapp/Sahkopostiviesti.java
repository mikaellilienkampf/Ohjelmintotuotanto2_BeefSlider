package com.bonuscarnisapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Quota;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class Sahkopostiviesti {
    private Context context;
    private String viestinLahettaja;
    private String sahkopostinSalasana;
    private String viestinVastaanottaja;
    private String viestinAihe;
    private String viestinSisalto;

    private String[] viestinLiitteet;


    // Parametriton konstruktori
    Sahkopostiviesti(){};

    // Parametrillinen konstruktori
    public Sahkopostiviesti(Context context, String viestinAihe, String viestinSisalto, String[] viestinLiitteet){
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

        // Viestin aihe, sisältö ja liitteet välitetään parametreina kun olio alustetaan
        this.viestinAihe = viestinAihe;
        this.viestinSisalto = viestinSisalto;
        this.viestinLiitteet = viestinLiitteet;

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

            // Liitetään varsinainen viestiosa ja liiteosa yhteen
            // Määritetään, että viesti koostuu useista osista (viesti + liitteet)
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart); // Lisätään varsinainen viestiosa
            // Luodaan viestiin liiteosat (tarvittaessa > 1)
            for(int i = 0; i < this.viestinLiitteet.length; i++){
                MimeBodyPart attachmentPart = new MimeBodyPart();
                File csvLiitteenKansio = new File(this.context.getFilesDir(), "csv_tiedostot");
                File liitetiedosto = new File(csvLiitteenKansio, this.viestinLiitteet[i]);
                //File liitetiedosto = new File(this.context.getFilesDir(), this.viestinLiitteet[i]);
                DataSource source = new FileDataSource(liitetiedosto);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(this.viestinLiitteet[i]);
                multipart.addBodyPart(attachmentPart); // Lisätään liiteosa muuhun viestiin
            }
            mimeMessage.setContent(multipart); // Kasataan varsinainen viesti





            /*
            // Kehitysvaiheessa: Luodaan testitiedosto getFilesDir()-hakemistoon
            String filename = "testiliite.txt";
            String fileContents = "Tämä on testitiedosto sähköpostiviestin liitteeksi!";
            try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/

            /*
            // Tällä voidaan listata Logcatiin getFilesDir()-kansiossa olevat tiedostot; helpottanee testausta
            File directory = new File(this.context.getFilesDir().toString());
            File[] files = directory.listFiles();
            Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName: " + files[i].getName());

                // Tällä poistettiin väärään paikkaan joutuneet csv-tiedostot
                //if(files[i].getName().endsWith(".csv")){
                //    files[i].delete();
                //}
            }*/

            /*
            // Tällä voidaan listata Logcatiin getFilesDir()/csv_tiedostot-kansiossa olevat tiedostot; helpottanee testausta
            File csvdirectory = new File(this.context.getFilesDir(), "csv_tiedostot");//.toString();
            File[] csvfiles = csvdirectory.listFiles();
            Log.d("csvFiles", "Size: "+ csvfiles.length);
            for (int j = 0; j < csvfiles.length; j++)
            {
                Log.d("csvFiles", "FileName: " + csvfiles[j].getName());
            }*/




            // Jotain määrityksiä...
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

            // Hoidetaan viestin lähetys uudessa säikeessä
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                        Handler threadHandler = new Handler(Looper.getMainLooper());
                        threadHandler.post(new Runnable() {
                            @Override
                            public void run(){
                                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                                alert.setTitle("Sähköpostin lähetys onnistui!");
                                alert.setMessage("Vastaanottajan sähköpostiosoite:\n" + stringReceiverEmail);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // ei tehdä mitään
                                    }
                                });
                                alert.show();
                            }
                        });
                    } catch (MessagingException e) {
                        e.printStackTrace();
                        Handler threadHandler = new Handler(Looper.getMainLooper());
                        threadHandler.post(new Runnable() {
                            @Override
                            public void run(){
                                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                                alert.setTitle("Sähköpostin lähetys epäonnistui!");
                                alert.setMessage("Tarkista sähköpostiosoite ja salasana.");
                                alert.setPositiveButton("Sulje", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // ei tehdä mitään
                                    }
                                });
                                alert.show();
                            }
                        });
                    }
                }
            });
            thread.start();
        } catch (MessagingException e) {
            e.printStackTrace();
            if (e instanceof AddressException) {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setTitle("Vastaanottajaa ei ole määritetty!");
                alert.setMessage("Aseta vastaanottajan osoite asetuksiin.");
                alert.setPositiveButton("Sulje", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // ei tehdä mitään
                    }
                });
                alert.show();
            }
        }

    }
}
