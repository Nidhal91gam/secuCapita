package com.technodev.capita.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import static com.twilio.rest.api.v2010.account.Message.creator;


public class SmsUtils {
    // il faut crée un Compt Twillo pour pouvoir implementer l'API pour envoyer des message
    public static final String FROM_NUMBER ="";
    public static final String SID_KEY = "";
    public static final String TOKEN_KEY = "";

    public static void snedSMS(String to, String messageBody){
        Twilio.init(SID_KEY, TOKEN_KEY);
        Message message = creator(new PhoneNumber(to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        System.out.println(message);
    }
}
