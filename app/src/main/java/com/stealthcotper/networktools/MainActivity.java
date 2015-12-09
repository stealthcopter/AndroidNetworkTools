package com.stealthcotper.networktools;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show();

        go();
      }
    });
  }


  private void go(){
    new Thread(new Runnable() {
      @Override public void run() {
        try {
          doPing();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void doPing() throws Exception{

    PingResult pingResult = Ping.onAddress("192.168.0.1").setTimeOutMillis(1000).doPing();

  }

}
