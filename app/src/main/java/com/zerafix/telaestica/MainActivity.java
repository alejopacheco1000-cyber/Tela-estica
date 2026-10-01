package com.zerafix.telaestica;

import android.app.*;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
    static final int ORANGE=Color.rgb(255,138,0), BG=Color.rgb(8,9,12), CARD=Color.rgb(19,22,28), MUTED=Color.rgb(150,155,165);
    LinearLayout root, content, nav;
    TextView status, profileTitle, resolutionValue, stretchValue;
    SeekBar stretch;
    EditText width, height;
    String selectedGame="Sin juego seleccionado", selectedPackage="";
    SharedPreferences prefs;

    int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    TextView tv(String s,float size,int color){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); return t; }
    GradientDrawable bg(int color,float r){ GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(r)); return g; }
    Button btn(String s){ Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setTextSize(13); b.setAllCaps(false); b.setBackground(bg(CARD,14)); b.setPadding(dp(12),0,dp(12),0); return b; }
    TextView section(String s){ TextView t=tv(s,12,MUTED); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setPadding(0,dp(14),0,dp(6)); return t; }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("profiles",0);
        buildShell();
        showHome();
    }

    void buildShell(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(18),dp(12),dp(18),dp(6));
        ImageView icon=new ImageView(this); icon.setImageResource(com.zerafix.telaestica.R.drawable.ic_tiger);
        head.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(44)));
        LinearLayout titles=new LinearLayout(this); titles.setOrientation(LinearLayout.VERTICAL); titles.setPadding(dp(10),0,0,0);
        TextView a=tv("TELA",22,Color.WHITE); a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView c=tv("ESTICA  •  TIGER EDITION",12,ORANGE); c.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        titles.addView(a); titles.addView(c); head.addView(titles,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView gear=tv("⚙",25,Color.WHITE); gear.setGravity(Gravity.CENTER); gear.setOnClickListener(v->showSettings());
        head.addView(gear,new LinearLayout.LayoutParams(dp(48),dp(48)));
        root.addView(head);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(16),0,dp(16),0);
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true); sc.addView(content); root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this); nav.setPadding(dp(8),dp(5),dp(8),dp(5)); nav.setGravity(Gravity.CENTER);
        String[] labels={"⌂  INICIO","▣  JUEGOS","⚙  AJUSTES"};
        View.OnClickListener[] ls={v->showHome(),v->showGames(),v->showSettings()};
        for(int i=0;i<3;i++){ TextView n=tv(labels[i],11,Color.WHITE); n.setGravity(Gravity.CENTER); n.setOnClickListener(ls[i]); nav.addView(n,new LinearLayout.LayoutParams(0,dp(50),1)); }
        root.addView(nav); setContentView(root);
    }

    void clear(){ content.removeAllViews(); }

    void showHome(){
        clear();
        TextView h=tv("Panel de control",24,Color.WHITE); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); content.addView(h);
        TextView sub=tv("Configura la pantalla para tus juegos",13,MUTED); content.addView(sub);
        content.addView(section("PERFIL ACTIVO"));
        LinearLayout game=card(); game.setPadding(dp(14),dp(12),dp(14),dp(12));
        LinearLayout gt=new LinearLayout(this); gt.setOrientation(LinearLayout.VERTICAL);
        profileTitle=tv(selectedGame,16,Color.WHITE); profileTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        gt.addView(profileTitle); gt.addView(tv(selectedPackage.length()>0?selectedPackage:"Selecciona un juego o perfil",11,MUTED));
        game.addView(gt,new LinearLayout.LayoutParams(0,dp(58),1));
        Button pick=btn("CAMBIAR"); pick.setOnClickListener(v->showGames()); game.addView(pick,new LinearLayout.LayoutParams(dp(105),dp(48)));
        content.addView(game);
        content.addView(section("RESOLUCIÓN PERSONALIZADA"));
        LinearLayout res=card(); res.setPadding(dp(14),dp(8),dp(14),dp(8));
        width=edit("1080"); height=edit("1920");
        res.addView(width,new LinearLayout.LayoutParams(0,dp(54),1)); TextView x=tv(" × ",18,MUTED); x.setGravity(Gravity.CENTER); res.addView(x); res.addView(height,new LinearLayout.LayoutParams(0,dp(54),1));
        content.addView(res);
        content.addView(section("ESTIRAMIENTO  < 70% — 100% >"));
        LinearLayout s=card(); s.setPadding(dp(14),dp(8),dp(14),dp(2));
        stretch=new SeekBar(this); stretch.setMax(30); stretch.setProgress(prefs.getInt("stretch",20)); stretch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean f){ int v=70+p; stretchValue.setText(v+"%"); }
            public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){}
        }); s.addView(stretch,new LinearLayout.LayoutParams(-1,dp(45)));
        stretchValue=tv((70+stretch.getProgress())+"%",15,ORANGE); stretchValue.setGravity(Gravity.CENTER); s.addView(stretchValue,new LinearLayout.LayoutParams(-1,dp(28)));
        content.addView(s);
        LinearLayout actions=new LinearLayout(this); actions.setPadding(0,dp(12),0,0);
        Button apply=btn("⚡  APLICAR AHORA"); apply.setTextColor(Color.BLACK); apply.setBackground(bg(ORANGE,14)); apply.setOnClickListener(v->applyNow());
        actions.addView(apply,new LinearLayout.LayoutParams(0,dp(56),1)); content.addView(actions);
        Button reset=btn("RESTAURAR PANTALLA NORMAL"); reset.setOnClickListener(v->resetDisplay()); content.addView(reset,new LinearLayout.LayoutParams(-1,dp(52)));
        status=tv("Estado: listo",12,MUTED); status.setGravity(Gravity.CENTER); status.setPadding(0,dp(12),0,dp(12)); content.addView(status);
    }

    LinearLayout card(){ LinearLayout l=new LinearLayout(this); l.setGravity(Gravity.CENTER_VERTICAL); l.setBackground(bg(CARD,16)); l.setPadding(dp(4),dp(4),dp(4),dp(4)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(8); l.setLayoutParams(p); return l; }
    EditText edit(String v){ EditText e=new EditText(this); e.setText(v); e.setTextColor(Color.WHITE); e.setTextSize(16); e.setGravity(Gravity.CENTER); e.setSingleLine(); e.setSelectAllOnFocus(true); e.setBackground(bg(Color.rgb(29,32,39),12)); return e; }

    void showGames(){
        clear();
        TextView h=tv("Juegos",24,Color.WHITE); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); content.addView(h);
        content.addView(tv("Selecciona el juego al que quieres aplicar el perfil",13,MUTED));
        Button installed=btn("＋  AÑADIR / DETECTAR JUEGOS"); installed.setOnClickListener(v->gameDialog()); content.addView(installed,new LinearLayout.LayoutParams(-1,dp(52)));
        content.addView(section("PERFILES RÁPIDOS"));
        String[][] quick={{"Minecraft","1280 × 720","minecraft"},{"Free Fire","1440 × 720","com.dts.freefireth"},{"PUBG Mobile","1600 × 720","com.tencent.ig"},{"Call of Duty Mobile","1280 × 720","com.activision.callofduty.shooter"}};
        for(String[] q:quick){ LinearLayout c=card(); c.setPadding(dp(14),dp(10),dp(14),dp(10)); LinearLayout t=new LinearLayout(this); t.setOrientation(LinearLayout.VERTICAL); t.addView(tv(q[0],15,Color.WHITE)); t.addView(tv(q[1]+"  •  perfil estirado",11,MUTED)); c.addView(t,new LinearLayout.LayoutParams(0,dp(58),1)); Button use=btn("USAR"); use.setOnClickListener(v->{selectedGame=q[0]; selectedPackage=q[2]; widthValue(q[1].split(" × ")[0]); heightValue(q[1].split(" × ")[1]); showHome();}); c.addView(use,new LinearLayout.LayoutParams(dp(90),dp(44))); content.addView(c); }
        content.addView(section("JUEGO SELECCIONADO"));
        content.addView(tv(selectedGame+"\n"+(selectedPackage.length()>0?selectedPackage:"Sin paquete"),14,Color.WHITE));
    }

    void widthValue(String s){ prefs.edit().putString("width",s).apply(); }
    void heightValue(String s){ prefs.edit().putString("height",s).apply(); }

    void gameDialog(){
        PackageManager pm=getPackageManager(); Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> apps=pm.queryIntentActivities(i,PackageManager.MATCH_ALL);
        Collections.sort(apps,(a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        final ArrayList<ResolveInfoHolder> data=new ArrayList<>();
        for(android.content.pm.ResolveInfo r:apps){ if(r.activityInfo==null) continue; data.add(new ResolveInfoHolder(r.loadLabel(pm).toString(),r.activityInfo.packageName)); }
        String[] names=new String[data.size()]; for(int k=0;k<data.size();k++) names[k]=data.get(k).label;
        new AlertDialog.Builder(this).setTitle("Mis juegos").setMessage("Marca el juego que usará este perfil").setItems(names,(d,which)->{selectedGame=data.get(which).label; selectedPackage=data.get(which).pkg; showHome();}).setNegativeButton("Cancelar",null).show();
    }
    static class ResolveInfoHolder{String label,pkg; ResolveInfoHolder(String l,String p){label=l;pkg=p;}}

    void applyNow(){
        String w=width.getText().toString().trim(), h=height.getText().toString().trim();
        int pct=70+stretch.getProgress(); prefs.edit().putInt("stretch",stretch.getProgress()).putString("width",w).putString("height",h).apply();
        if(w.isEmpty()||h.isEmpty()){status.setText("Estado: introduce ancho y alto"); return;}
        int ww; try{ww=Integer.parseInt(w);}catch(Exception e){status.setText("Estado: resolución no válida");return;}
        int hh; try{hh=Integer.parseInt(h);}catch(Exception e){status.setText("Estado: resolución no válida");return;}
        int stretchedW=Math.max(240,Math.round(ww*pct/100f));
        status.setText("Aplicando "+stretchedW+" × "+hh+"  •  "+pct+"%...");
        runWm("wm size "+stretchedW+"x"+hh,()->runOnUiThread(()->{status.setText("✓ Pantalla estirada aplicada"); if(prefs.getBoolean("overlay",false)) startOverlayService();}));
    }

    void resetDisplay(){ status.setText("Restaurando pantalla..."); runWm("wm size reset",()->runOnUiThread(()->status.setText("✓ Pantalla normal restaurada"))); }

    void runWm(String cmd,Runnable done){
        new Thread(()->{
            try{
                boolean use=prefs.getBoolean("shizuku",true);
                if(!use || !Shizuku.pingBinder()){ runOnUiThread(()->status.setText("Necesitas activar Shizuku")); return; }
                if(Shizuku.checkSelfPermission()!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(this::requestShizuku); return; }
                Process p=Shizuku.newProcess(new String[]{"sh","-c",cmd},null,null);
                p.waitFor(); done.run();
            }catch(Exception e){ runOnUiThread(()->status.setText("Error: "+e.getMessage())); }
        }).start();
    }

    void requestShizuku(){ try{ Shizuku.requestPermission(100); }catch(Exception e){ Toast.makeText(this,"Abre Shizuku y autoriza Tela Estica",Toast.LENGTH_LONG).show(); } }

    void showSettings(){
        clear();
        TextView h=tv("Ajustes",24,Color.WHITE); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); content.addView(h);
        content.addView(tv("Controla el acceso y el comportamiento de la app",13,MUTED));
        content.addView(section("ACCESO DEL SISTEMA"));
        LinearLayout sh=card(); TextView st=tv("Shizuku\nPermite cambiar la resolución con privilegios ADB",15,Color.WHITE); sh.addView(st,new LinearLayout.LayoutParams(0,dp(70),1));
        Button sb=btn(Shizuku.pingBinder()?"AUTORIZAR":"ABRIR"); sb.setOnClickListener(v->{if(Shizuku.pingBinder()) requestShizuku(); else Toast.makeText(this,"Inicia Shizuku primero",Toast.LENGTH_LONG).show();}); sh.addView(sb,new LinearLayout.LayoutParams(dp(105),dp(46))); content.addView(sh);
        content.addView(section("OVERLAY EN JUEGO"));
        LinearLayout ov=card(); TextView ot=tv("Indicador flotante\nMuestra cuando Tela Estica está activa",15,Color.WHITE); ov.addView(ot,new LinearLayout.LayoutParams(0,dp(70),1));
        Switch sw=new Switch(this); sw.setChecked(prefs.getBoolean("overlay",false)); sw.setOnCheckedChangeListener((b,c)->{prefs.edit().putBoolean("overlay",c).apply(); if(c){if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));} else startOverlayService();} else stopService(new Intent(this,OverlayService.class));}); ov.addView(sw); content.addView(ov);
        content.addView(section("PANTALLA"));
        Button nativeBtn=btn("USAR RESOLUCIÓN NATIVA"); nativeBtn.setOnClickListener(v->{android.util.DisplayMetrics m=getResources().getDisplayMetrics(); widthValue(String.valueOf(m.widthPixels)); heightValue(String.valueOf(m.heightPixels)); Toast.makeText(this,"Resolución nativa cargada",Toast.LENGTH_SHORT).show();}); content.addView(nativeBtn,new LinearLayout.LayoutParams(-1,dp(52)));
        Button info=btn("ACERCA DE TELA ESTICA TIGER"); info.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Tela Estica • Tiger Edition").setMessage("Perfiles de juegos\nResolución personalizada\nEstiramiento 70–100%\nAplicar / restaurar\nShizuku y overlay").setPositiveButton("OK",null).show()); content.addView(info,new LinearLayout.LayoutParams(-1,dp(52)));
    }

    void startOverlayService(){ if(Build.VERSION.SDK_INT<23 || Settings.canDrawOverlays(this)) startService(new Intent(this,OverlayService.class)); }

    @Override protected void onResume(){ super.onResume(); }
}
