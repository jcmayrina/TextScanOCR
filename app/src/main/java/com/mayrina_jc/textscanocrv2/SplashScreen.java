package com.mayrina_jc.textscanocrv2;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends MainActivity{
    Intent CallMain;
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        CallMain = new Intent(".MainActivity");

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }finally {
                    startActivity(CallMain);
                    finish();
                }
            }
        };
        timer.start();
    }
}
