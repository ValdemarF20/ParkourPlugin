package net.valdemarf.parkourplugin;

public final class Formatter {
                        /*
int totalTime = 77523;
Duration timeLeft = Duration.ofMillis(totalTime);
String hhmmss = String.format("%02d:%02d:%02d.%03d",
                timeLeft.toHours(),
                timeLeft.toMinutesPart(),
                timeLeft.toSecondsPart(),
                timeLeft.toMillisPart());

System.out.println(hhmmss);
                        */

    public static String formatTime(long time) {
        long seconds = time / 1000;
        long millis = time % 1000;

        return seconds + "." + String.format("%03d", millis)+ "s";
    }
}
