package org.launcher.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.launcher.config.ConfigurationControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class PasswordManager {
    private static final Logger logger = LoggerFactory.getLogger(PasswordManager.class);
    private static final String defaultPassword= "$argon2i$v=19$m=32768,t=3,p=1$CuycojSjDirD4L7pMuaNqw$QqaTlxxgWdP4s8FYwpsPI4z6vIllbXFd3p3ixJCpGFg";
    private static ConfigurationControl configurationControl;
    private static final Argon2 argon2 = Argon2Factory.create();
    private static final Pattern hash_pattern = Pattern.compile("^\\$argon2(?:id|i|d)\\$v=\\d+\\$m=\\d+,t=\\d+,p=\\d+\\$.+\\$.+$");

    public static void inititalize(ConfigurationControl conf) {
        configurationControl = conf;
    }

    public static boolean isPasswordValid(String password) {
        return isPasswordHash() ?
                argon2.verify(configurationControl.getConfiguration().getAdmin().getPassword(), password.toCharArray()) :
                argon2.verify(defaultPassword, password.toCharArray()); //argon2.verify(configurationControl.getConfiguration().getAdmin().getPassword(), password.toCharArray());
    }

    public static boolean isPasswordHash(){
       return hash_pattern.matcher(configurationControl.getConfiguration().getAdmin().getPassword()).matches();
    }

    public static void setPassword(String password) {
        String pw = encodePassword(password);
       // System.out.println(hash);
        configurationControl.getConfiguration().getAdmin().setPassword(pw);
        configurationControl.writeNewPassword();
    }

    private static String encodePassword(String password) {
        byte[] pass = password.getBytes();
        return argon2.hash(
                3,          // iterations
                32768,      // memory
                1,          // parallelism
                pass
        );
    }
}
