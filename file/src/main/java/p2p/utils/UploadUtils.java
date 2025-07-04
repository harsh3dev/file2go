package p2p.utils;

import java.util.Random;

public class UploadUtils {
    public static int getPort(){
        int START_PORT = 49152;
        int END_PORT = 65535;

        Random rand = new Random();

        return rand.nextInt(END_PORT - START_PORT) + START_PORT;
    }
}
