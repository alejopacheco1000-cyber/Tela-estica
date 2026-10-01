package com.zerafix.telaestica;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;
import rikka.shizuku.Shizuku;

public class OverlayService extends Service {
    WindowManager wm;LinearLayout panel;SeekBar bar;TextView pct;Handler h=new Handler(Looper.getMainLooper());String last="";
    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(){super.onCreate();channel();startForeground(7,notification());if(Settings.canDrawOverlays(this))panel();h.post(check);}
    void channel(){if(Build.VERSION.SDK_INT>=26){NotificationManager n=getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel("tela_estica","Tela Estica",NotificationManager.IMPORTANCE_LOW));}}
    Notification notification(){Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"tela_estica"):new Notification.Builder(this);return b.setContentTitle("Tela Estica activa").setContentText("Controles flotantes activos").setSmallIcon(android.R.drawable.ic_menu_crop).build();}
    void panel(){
        wm=getSystemService(WindowManager.class);panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(5),dp(5),dp(5),dp(5));panel.setBackgroundColor(Color.argb(225,20,20,28));
        pct=new TextView(this);pct.setTextColor(Color.WHITE);pct.setTextSize(12);pct.setGravity(Gravity.CENTER);bar=new SeekBar(this);bar.setMax(119);
        int cur=getSharedPreferences(MainActivity.PREFS,0).getInt("stretch",110);bar.setProgress(cur-80);pct.setText(cur+"%");
        panel.addView(pct,new LinearLayout.LayoutParams(dp(62),dp(30)));panel.addView(bar,new LinearLayout.LayoutParams(dp(62),dp(175)));
        TextView close=new TextView(this);close.setText("×");close.setTextColor(Color.WHITE);close.setTextSize(22);close.setGravity(Gravity.CENTER);close.setOnClickListener(v->stopSelf());panel.addView(close,new LinearLayout.LayoutParams(dp(62),dp(35)));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean from){if(!from)return;int v=80+p;getSharedPreferences(MainActivity.PREFS,0).edit().putInt("stretch",v).apply();pct.setText(v+"%");new Thread(()->stretch(v)).start();}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;WindowManager.LayoutParams lp=new WindowManager.LayoutParams(dp(72),dp(250),type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);lp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;wm.addView(panel,lp);
    }
    int[] size(){android.graphics.Point p=new android.graphics.Point();getSystemService(WindowManager.class).getDefaultDisplay().getRealSize(p);return new int[]{p.x,p.y};}
    void stretch(int v){int[] s=size();int w=Math.max(480,Math.round(s[0]*100f/v));run("wm size "+w+"x"+s[1]);}
    void run(String cmd){if(Shizuku.pingBinder()&&Shizuku.checkSelfPermission()==0){try{java.lang.reflect.Method m=Shizuku.class.getDeclaredMethod("newProcess",String[].class,String[].class,String.class);Object p=m.invoke(null,new String[]{"sh","-c",cmd},null,null);p.getClass().getMethod("waitFor").invoke(p);p.getClass().getMethod("destroy").invoke(p);return;}catch(Exception ignored){}}try{Runtime.getRuntime().exec(new String[]{"su","-c",cmd}).waitFor();}catch(Exception ignored){}}
    Runnable check=new Runnable(){public void run(){try{UsageStatsManager u=getSystemService(UsageStatsManager.class);long n=System.currentTimeMillis();List<UsageStats> a=u.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,n-15000,n);UsageStats best=null;for(UsageStats x:a)if(best==null||x.getLastTimeUsed()>best.getLastTimeUsed())best=x;if(best!=null){String p=best.getPackageName();if(!p.equals(getPackageName())&&!p.equals(last)){last=p;boolean on=getSharedPreferences(MainActivity.PREFS,0).getBoolean("game_"+p,false);if(on)new Thread(()->stretch(getSharedPreferences(MainActivity.PREFS,0).getInt("stretch",110))).start();}}}catch(Exception ignored){}h.postDelayed(this,1500);}};
    @Override public void onDestroy(){h.removeCallbacks(check);if(wm!=null&&panel!=null)try{wm.removeView(panel);}catch(Exception ignored){}new Thread(()->run("wm size reset")).start();super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
