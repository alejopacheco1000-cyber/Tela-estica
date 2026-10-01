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
    TextView status, profileTitle, resolutionValue, stretchValue, cropValue;
    SeekBar stretch, crop;
    EditText width, height, dpi;
    String selectedGame="Sin juego seleccionado", selectedPackage="";
    Spinner modeSpinner;
    int ORANGE2=Color.rgb(255,170,35);
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
        TextView a=tv("CAT",22,Color.WHITE); a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView c=tv("RESOLUTION  •  Y9 PRIME",12,ORANGE); c.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
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
        content.addView(tv("Resolución y pantalla estirada para tus juegos",13,MUTED));
        content.addView(section("PERFIL ACTIVO"));
        LinearLayout game=card(); game.setPadding(dp(14),dp(10),dp(14),dp(10));
        LinearLayout gt=new LinearLayout(this); gt.setOrientation(LinearLayout.VERTICAL);
        profileTitle=tv(selectedGame,16,Color.WHITE); profileTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        gt.addView(profileTitle); gt.addView(tv(selectedPackage.length()>0?selectedPackage:"Selecciona un juego o perfil",11,MUTED));
        game.addView(gt,new LinearLayout.LayoutParams(0,dp(58),1));
        Button pick=btn("CAMBIAR"); pick.setOnClickListener(v->showGames()); game.addView(pick,new LinearLayout.LayoutParams(dp(105),dp(48)));
        content.addView(game);

        content.addView(section("RESOLUCIÓN"));
        LinearLayout res=card(); res.setPadding(dp(14),dp(8),dp(14),dp(8));
        width=edit(prefs.getString("width","1080")); height=edit(prefs.getString("height","1920"));
        res.addView(width,new LinearLayout.LayoutParams(0,dp(54),1)); TextView x=tv(" × ",18,MUTED); x.setGravity(Gravity.CENTER); res.addView(x); res.addView(height,new LinearLayout.LayoutParams(0,dp(54),1));
        content.addView(res);

        LinearLayout presetRow=new LinearLayout(this); String[] presets={"16:9","18:9","19.5:9","Ultra"}; 
        for(String p:presets){ Button b=btn(p); b.setTextSize(11); b.setOnClickListener(v->applyPreset(p)); presetRow.addView(b,new LinearLayout.LayoutParams(0,dp(44),1)); } content.addView(presetRow);

        content.addView(section("ESTIRAMIENTO"));
        LinearLayout s=card(); s.setPadding(dp(14),dp(5),dp(14),dp(2));
        stretch=new SeekBar(this); stretch.setMax(40); stretch.setProgress(prefs.getInt("stretch",20)); 
        stretch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar b,int p,boolean f){stretchValue.setText((60+p)+"%");} public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){}});
        s.addView(stretch,new LinearLayout.LayoutParams(-1,dp(42))); stretchValue=tv((60+stretch.getProgress())+"%",15,ORANGE); stretchValue.setGravity(Gravity.CENTER); s.addView(stretchValue,new LinearLayout.LayoutParams(-1,dp(28))); content.addView(s);

        content.addView(section("RECORTE LATERAL"));
        LinearLayout cr=card(); cr.setPadding(dp(14),dp(5),dp(14),dp(2));
        crop=new SeekBar(this); crop.setMax(30); crop.setProgress(prefs.getInt("crop",0)); 
        crop.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar b,int p,boolean f){cropValue.setText(p+"%");} public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){}});
        cr.addView(crop,new LinearLayout.LayoutParams(-1,dp(42))); cropValue=tv(crop.getProgress()+"%",15,ORANGE); cropValue.setGravity(Gravity.CENTER); cr.addView(cropValue,new LinearLayout.LayoutParams(-1,dp(28))); content.addView(cr);

        content.addView(section("DPI / DENSIDAD"));
        dpi=edit(prefs.getString("dpi","")); dpi.setHint("Automático"); dpi.setTextColor(Color.WHITE); dpi.setHintTextColor(MUTED); content.addView(dpi,new LinearLayout.LayoutParams(-1,dp(52)));

        content.addView(section("ACCESO"));
        modeSpinner=new Spinner(this); ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Shizuku","Root"}); modeSpinner.setAdapter(ad); modeSpinner.setSelection(prefs.getBoolean("root",false)?1:0); modeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){prefs.edit().putBoolean("root",pos==1).apply();} public void onNothingSelected(android.widget.AdapterView<?> p){}}); content.addView(modeSpinner,new LinearLayout.LayoutParams(-1,dp(50)));

        Button apply=btn("⚡  APLICAR RESOLUCIÓN"); apply.setTextColor(Color.BLACK); apply.setBackground(bg(ORANGE,14)); apply.setOnClickListener(v->applyNow()); content.addView(apply,new LinearLayout.LayoutParams(-1,dp(56)));
        Button reset=btn("↺  RESTAURAR PANTALLA ORIGINAL"); reset.setOnClickListener(v->resetDisplay()); content.addView(reset,new LinearLayout.LayoutParams(-1,dp(52)));
        status=tv("Estado: listo",12,MUTED); status.setGravity(Gravity.CENTER); status.setPadding(0,dp(10),0,dp(12)); content.addView(status);
    }

    void applyPreset(String p){
        int w=1080,h=1920;
        if(p.equals("16:9")){w=1080;h=1920;} else if(p.equals("18:9")){w=1080;h=2160;} else if(p.equals("19.5:9")){w=1080;h=2340;} else {w=1280;h=2560;}
        width.setText(String.valueOf(w)); height.setText(String.valueOf(h)); Toast.makeText(this,"Preset "+p+" cargado",Toast.LENGTH_SHORT).show();
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
        int pct=60+stretch.getProgress(), cropPct=crop.getProgress();
        prefs.edit().putInt("stretch",stretch.getProgress()).putInt("crop",cropPct).putString("width",w).putString("height",h).putString("dpi",dpi.getText().toString()).apply();
        if(w.isEmpty()||h.isEmpty()){status.setText("Estado: introduce ancho y alto"); return;}
        try{
            int ww=Integer.parseInt(w), hh=Integer.parseInt(h);
            int stretchedW=Math.max(240,Math.round(ww*pct/100f));
            if(cropPct>0) stretchedW=Math.max(240,stretchedW-Math.round(stretchedW*cropPct/100f));
            String cmd="wm size "+stretchedW+"x"+hh;
            String d=dpi.getText().toString().trim(); if(!d.isEmpty()) cmd+=" && wm density "+Integer.parseInt(d);
            status.setText("Aplicando "+stretchedW+" × "+hh+" • "+pct+"%...");
            runCommand(cmd,()->runOnUiThread(()->{status.setText("✓ Pantalla aplicada"); if(prefs.getBoolean("overlay",false)) startOverlayService();}));
        }catch(Exception e){status.setText("Estado: valores no válidos");}
    }

    void resetDisplay(){
        status.setText("Restaurando pantalla...");
        runCommand("wm size reset && wm density reset",()->runOnUiThread(()->status.setText("✓ Pantalla normal restaurada")));
    }

    void runCommand(String cmd,Runnable done){
        new Thread(()->{
            try{
                boolean rootMode=prefs.getBoolean("root",false);
                if(rootMode){
                    java.lang.Process p=new ProcessBuilder("su","-c",cmd).redirectErrorStream(true).start();
                    int code=p.waitFor(); if(code!=0) throw new RuntimeException("Root rechazó el comando");
                }else{
                    if(!Shizuku.pingBinder()){runOnUiThread(()->status.setText("Necesitas iniciar Shizuku")); return;}
                    if(Shizuku.checkSelfPermission()!=PackageManager.PERMISSION_GRANTED){runOnUiThread(this::requestShizuku); return;}
                    java.lang.reflect.Method m=Shizuku.class.getDeclaredMethod("newProcess",String[].class,String[].class,String.class); m.setAccessible(true);
                    Object rp=m.invoke(null,new Object[]{new String[]{"sh","-c",cmd},null,null});
                    java.lang.reflect.Method wait=rp.getClass().getMethod("waitFor"); int code=(Integer)wait.invoke(rp); if(code!=0) throw new RuntimeException("Comando rechazado");
                }
                done.run();
            }catch(Exception e){runOnUiThread(()->status.setText("Error: "+e.getMessage()));}
        }).start();
    }

    void requestShizuku(){
        try{ Shizuku.requestPermission(100); }
        catch(Exception e){ Toast.makeText(this,"Abre Shizuku y autoriza Tela Estica",Toast.LENGTH_LONG).show(); }
    }

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
        Button info=btn("ACERCA DE CAT RESOLUTION Y9"); info.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Cat Resolution • Y9 Prime").setMessage("Perfiles de juegos\nResolución personalizada\nEstiramiento 70–100%\nAplicar / restaurar\nShizuku y overlay").setPositiveButton("OK",null).show()); content.addView(info,new LinearLayout.LayoutParams(-1,dp(52)));
    }

    void startOverlayService(){ if(Build.VERSION.SDK_INT<23 || Settings.canDrawOverlays(this)) startService(new Intent(this,OverlayService.class)); }

    @Override protected void onResume(){ super.onResume(); }
}
