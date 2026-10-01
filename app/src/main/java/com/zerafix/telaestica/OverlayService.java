package com.zerafix.telaestica;
import android.app.Service;import android.content.Intent;import android.os.IBinder;import android.graphics.Color;import android.graphics.PixelFormat;import android.view.*;import android.widget.*;
public class OverlayService extends Service {
 WindowManager wm; View panel;
 public IBinder onBind(Intent i){return null;}
 public int onStartCommand(Intent i,int f,int id){if(panel==null){wm=(WindowManager)getSystemService(WINDOW_SERVICE); LinearLayout l=new LinearLayout(this);l.setPadding(24,16,24,16);l.setBackgroundColor(Color.argb(235,15,18,23));TextView t=new TextView(this);t.setText("Tela Estica • activo");t.setTextColor(Color.WHITE);t.setTextSize(16);l.addView(t);panel=l;int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;WindowManager.LayoutParams p=new WindowManager.LayoutParams(-2,-2,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;wm.addView(panel,p);}return START_STICKY;}
 public void onDestroy(){if(panel!=null)wm.removeView(panel);super.onDestroy();}
}