

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


import org.apache.commons.codec.binary.Base64;

public class SMTPClient {

    private HelperLogger logger = HelperLogger.getLogger(SMTPClient.class);

    private final String smtpName;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;

    public SMTPClient(String smtpName, int smtpPort, String smtpUsername, String smtpPassword) {
        this.smtpName = smtpName;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public boolean sendMessage(ModelMailMessage mailMessage) {
        boolean sentOk = false;

        try (Socket socket = new Socket(smtpName, smtpPort);
             DataOutputStream os = new DataOutputStream(socket.getOutputStream());
             BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            if (smtpUsername != null && smtpPassword != null) {
                writeOut(os, "EHLO ShiraYehezkia AlonLavi\r\n");
                writeOut(os, "AUTH LOGIN\r\n");
                writeOut(os, Base64.encodeBase64String(smtpUsername.getBytes()) + "\r\n");
                writeOut(os, Base64.encodeBase64String(smtpPassword.getBytes()) + "\r\n");
            } else {
                writeOut(os, "HELO ShiraYehezkia AlonLavi\r\n");
            }
            writeOut(os, "MAIL FROM: " + mailMessage.getFrom() + "\r\n");
            writeOut(os, "RCPT TO: " + mailMessage.getTo() + "\r\n");
            writeOut(os, "DATA\r\n");
            writeOut(os, "Subject: " + mailMessage.getSubject() + "\r\n");
            writeOut(os, "From: Mr. Tasker\r\n");
            writeOut(os, "Sender: " + mailMessage.getFrom() + "\r\n");
            writeOut(os, "\r\n");
            writeOut(os, mailMessage.getMessage().replaceAll("[^\r]\n", "\r\n") + "\r\n");
            writeOut(os, ".\r\n");
            writeOut(os, "QUIT\r\n");

            String response;
            while ((response = is.readLine()) != null) {
                logger.debug("Received: " + response);
                if (response.toUpperCase().indexOf("QUEUED") != -1) {
                    sentOk = true;
                }
            }

        } catch (IOException | InterruptedException e) {
            sentOk = false;
        }

        if (sentOk) {
            logger.info(String.format("Mail message from: '%s', to: '%s' sent OK.", mailMessage.getFrom(), mailMessage.getTo()));
        } else {
            logger.error(String.format("Error while sending mail message from: %s, to: %s", mailMessage.getFrom(), mailMessage.getTo()));
        }
        return sentOk;
    }

    public boolean sendSMS(String to, String message) {
        return sendMessage(new ModelMailMessage("networks2014@gmail.com", "networks2014@walla.com", to, message));
    }

    private void writeOut(DataOutputStream os, String s) throws IOException, InterruptedException {
        logger.debug("Writing: " + s);
        os.writeBytes(s);
        Thread.sleep(250);
    }

}
