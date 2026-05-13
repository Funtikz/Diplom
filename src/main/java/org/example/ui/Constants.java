package org.example.ui;

public class Constants {
    public static class Capability{
        public final static String ENV = System.getProperty("env") ;
        public final static String USER_1_LOGIN = System.getProperty("login.1");
        public final static String USER_1_PASS = System.getProperty("pass.1");
        public final static Boolean NEED_VIDEO = Boolean.valueOf(System.getProperty("isVideoRecord"));
        public final static String BROWSER = System.getProperty("browser");

    }

}
