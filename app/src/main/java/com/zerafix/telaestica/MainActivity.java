package com.zerafix.telaestica;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
    static final String PREFS="tela_estica";
    LinearLayout box; TextView status,value;
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    android.content.SharedPreferences prefs(){return getSharedPreferences(PREFS,0);}
    int stretch(){return prefs().getInt("stretch",110);}
    void stretch(int v){prefs().edit().putInt("stretch",v).apply();}
    boolean game(String p){return prefs().getBoolean("game_"+p,false);}
    void game(String p,boolean v){prefs().edit().putBoolean("game_"+p,v).apply();}
    boolean useShizuku(){return prefs().getBoolean("shizuku",true);}
    TextView text(String s,float z){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(z);t.setPadding(dp(14),dp(10),dp(14),dp(10));return t;}
    Button button(String s){Button b=new Button(this);b.setText(s);return b;}

    @Override public void onCreate(Bundle b){super.onCreate(b);build();requestShizuku();}
    void build(){
        box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(18),dp(14),dp(10));box.setBackgroundColor(Color.rgb(9,11,16));
        box.addView(text("TELA ESTICA",26));
        TextView sub=text("Pantalla estirada para juegos • 80%–199%",14);sub.setTextColor(Color.LTGRAY);box.addView(sub);
        status=text("Estado: listo",14);box.addView(status);
        LinearLayout tabs=new LinearLayout(this);Button games=button("JUEGOS"),settings=button("AJUSTES");
        tabs.addView(games,new LinearLayout.LayoutParams(0,dp(52),1));tabs.addView(settings,new LinearLayout.LayoutParams(0,dp(52),1));box.addView(tabs);
        games.setOnClickListener(v->showGames());settings.setOnClickListener(v->showSettings());showGames();setContentView(box);
    }
    void clear(){while(box.getChildCount()>4)box.removeViewAt(4);}
    void showGames(){
        clear();box.addView(text("Mis juegos",20));TextView info=text("Marca los juegos que usarán el perfil.",13);info.setTextColor(Color.LTGRAY);box.addView(info);
        ScrollView scroll=new ScrollView(this);LinearLayout listBox=new LinearLayout(this);listBox.setOrientation(LinearLayout.VERTICAL);
        PackageManager pm=getPackageManager();Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list=pm.queryIntentActivities(i,0);Collections.sort(list,(a,c)->a.loadLabel(pm).toString().compareToIgnoreCase(c.loadLabel(pm).toString()));
        for(ResolveInfo r:list){String pkg=r.activityInfo.packageName;if(pkg.equals(getPackageName()))continue;CheckBox cb=new CheckBox(this);cb.setText(r.loadLabel(pm));cb.setTextColor(Color.WHITE);cb.setTextSize(15);cb.setChecked(game(pkg));cb.setOnCheckedChangeListener((v,x)->game(pkg,x));listBox.addView(cb);}
        scroll.addView(listBox);box.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button ov=button("ACTIVAR CONTROLES FLOTANTES");ov.setOnClickListener(v->startOverlay());box.addView(ov);
    }
    void showSettings(){
        clear();box.addView(text("Ajustes",20));value=text("Estiramiento: "+stretch()+"%",18);box.addView(value);
        SeekBar seek=new SeekBar(this);seek.setMax(119);seek.setProgress(stretch()-80);box.addView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){int v=80+p;stretch(v);value.setText("Estiramiento: "+v+"%");}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        Switch sh=new Switch(this);sh.setText("Usar Shizuku cuando esté disponible");sh.setTextColor(Color.WHITE);sh.setChecked(useShizuku());sh.setOnCheckedChangeListener((v,x)->prefs().edit().putBoolean("shizuku",x).apply());box.addView(sh);
        Button a=button("APLICAR AHORA");a.setOnClickListener(v->applyNow());box.addView(a);
        Button r=button("RESTAURAR PANTALLA NORMAL");r.setOnClickListener(v->reset());box.addView(r);
        Button o=button("PERMISO DE VENTANA FLOTANTE");o.setOnClickListener(v->{if(!Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));});box.addView(o);
        Button u=button("PERMISO PARA DETECTAR JUEGO ACTIVO");u.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));box.addView(u);
    }
    String targetSize(int percent){android.graphics.Point p=new android.graphics.Point();getWindowManager().getDefaultDisplay().getRealSize(p);int w=Math.max(480,Math.round(p.x*100f/percent));return w+"x"+p.y;}
    void applyNow(){new Thread(()->{Result r=runCommand("wm size "+targetSize(stretch()));runOnUiThread(()->status.setText(r.ok?"Estado: "+stretch()+"% aplicado":"Error: "+r.msg));}).start();}
    void reset(){new Thread(()->{Result r=runCommand("wm size reset");runOnUiThread(()->status.setText(r.ok?"Estado: pantalla normal":"Error: "+r.msg));}).start();}
    void startOverlay(){if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));return;}Intent s=new Intent(this,OverlayService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(s);else startService(s);status.setText("Controles flotantes activados");}
    void requestShizuku(){try{Shizuku.addRequestPermissionResultListener((c,r)->status.setText(r==0?"Shizuku autorizado":"Shizuku: permiso rechazado"));if(Shizuku.pingBinder()&&Shizuku.checkSelfPermission()!=PackageManager.PERMISSION_GRANTED)Shizuku.requestPermission(100);}catch(Exception ignored){}}
    Result runCommand(String cmd){if(useShizuku()&&Shizuku.pingBinder()&&Shizuku.checkSelfPermission()==PackageManager.PERMISSION_GRANTED){try{Method m=Shizuku.class.getDeclaredMethod("newProcess",String[].class,String[].class,String.class);Object p=m.invoke(null,new String[]{"sh","-c",cmd},null,null);InputStream in=(InputStream)p.getClass().getMethod("getInputStream").invoke(p);String out=read(in);int code=(Integer)p.getClass().getMethod("waitFor").invoke(p);p.getClass().getMethod("destroy").invoke(p);return new Result(code==0,out);}catch(Exception e){return new Result(false,"Shizuku: "+e.getMessage());}}try{java.lang.Process p=new ProcessBuilder("su","-c",cmd).redirectErrorStream(true).start();String out=read(p.getInputStream());int code=p.waitFor();return new Result(code==0,out);}catch(Exception e){return new Result(false,"Root no disponible: "+e.getMessage());}}
    static String read(InputStream in)throws Exception{BufferedReader r=new BufferedReader(new InputStreamReader(in));StringBuilder b=new StringBuilder();String s;while((s=r.readLine())!=null)b.append(s).append('\n');return b.toString().trim();}
    static class Result{boolean ok;String msg;Result(boolean o,String m){ok=o;msg=m;}}
}
