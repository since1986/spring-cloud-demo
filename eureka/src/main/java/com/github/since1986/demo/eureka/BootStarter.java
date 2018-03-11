package com.github.since1986.demo.eureka;

import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class BootStarter {

    private volatile boolean started;


    //用于后续Spring Boot操作的回调
    public interface Callback {
        void bootRun();
    }

    private boolean enableAutomaticallyStart = true;
    private int automaticallyStartDelay = 3;

    public boolean isEnableAutomaticallyStart() {
        return enableAutomaticallyStart;
    }

    public void setEnableAutomaticallyStart(boolean enableAutomaticallyStart) {
        this.enableAutomaticallyStart = enableAutomaticallyStart;
    }

    public int getAutomaticallyStartDelay() {
        return automaticallyStartDelay;
    }

    public void setAutomaticallyStartDelay(int automaticallyStartDelay) {
        this.automaticallyStartDelay = automaticallyStartDelay;
    }

    public void startup(boolean enableAutomaticallyStart, int automaticallyStartDelay, Callback callback) {
        if (StringUtils.isBlank(System.getProperty("spring.profiles.active"))) { //如果没有通过参数spring.profiles.active设置active profile则让用户在控制台自己选择
            System.out.println("***Please choose active profile:***\n\tp: production\n\td: development");

            Timer timer = new Timer();
            if (enableAutomaticallyStart && System.getProperty("os.name").lastIndexOf("Linux") == -1) { //如果当前操作系统环境为非Linux环境(一般为开发环境)则automaticallyStartDelay秒后自动设置为开发环境
                System.out.printf("\nSystem will automatically select 'd' in %d seconds.\n", automaticallyStartDelay);
                timer.scheduleAtFixedRate(new TimerTask() {

                    private ThreadLocal<Integer> countDown = ThreadLocal.withInitial(() -> automaticallyStartDelay);

                    @Override
                    public void run() {
                        if (countDown.get() == 0) {
                            timer.cancel();
                            started = true;

                            System.setProperty("spring.profiles.active", "development");
                            callback.bootRun();
                        }
                        countDown.set(countDown.get() - 1);
                    }
                }, 0, 1000);
            }

            try (Scanner scanner = new Scanner(System.in)) {
                Pattern pattern = Pattern.compile("^p|d$");
                //如果是Linux系统(一般为生产环境)则强制等待用户输入(一般是忘记设置spring.profiles.active了，这等于给了设置active profile的"second chance")
                while (scanner.hasNextLine()) {
                    if (started) {
                        break;
                    }
                    String line = scanner.nextLine();
                    if (!pattern.matcher(line).find()) {
                        System.out.println("INVALID INPUT!");
                    } else {
                        timer.cancel();
                        System.setProperty("spring.profiles.active", line.equals("d") ? "development" : "production");
                        callback.bootRun();
                        break;
                    }
                }
            }
        } else { //如果已经通过参数spring.profiles.active设置了active profile直接启动
            callback.bootRun();
        }
    }

    public void startup(Callback callback) {
        startup(this.enableAutomaticallyStart, this.automaticallyStartDelay, callback);
    }
}
