package com.zerafix.telaestica;
import android.app.*;import android.os.*;import android.content.*;import android.view.*;import android.widget.*;import rikka.shizuku.Shizuku;
public class MainActivity extends Activity {
 TextView status,log; EditText width,height; Spinner games; static final int REQ=42;
 String[] gameNames={"Seleccionar juego","Minecraft","Free Fire","PUBG Mobile","Call of Duty Mobile","Personalizado"};
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);status=findViewById(R.id.status);log=findViewById(R.id.log);width=findViewById(R.id.width);height=findViewById(R.id.height);games=findViewById(R.id.gameSpinner);games.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,gameNames));findViewById(R.id.apply).setOnClickListener(v->apply());findViewById(R.id.restore).setOnClickListener(v->restore());checkShizuku();}
 void checkShizuku(){try{if(Shizuku.pingBinder()){if(Shizuku.checkSelfPermission()==0)status.setText("Shizuku: conectado");else{status.setText("Shizuku: permiso requerido");Shizuku.requestPermission(REQ);}}else status.setText("Shizuku: no iniciado");}catch(Exception e){status.setText("Shizuku: no disponible");}}
 void apply(){int w=toInt(width),h=toInt(height);if(w<320||h<240){log.setText("Resolución no válida.");return;}runShizuku("wm size "+w+"x"+h);}
 void restore(){runShizuku("wm size reset");}
 int toInt(EditText e){try{return Integer.parseInt(e.getText().toString());}catch(Exception x){return 0;}}
 void runShizuku(String cmd){new Thread(()->{try{if(!Shizuku.pingBinder()||Shizuku.checkSelfPermission()!=0){runOnUiThread(()->log.setText("Concede el permiso de Shizuku primero."));return;}Process p=Shizuku.newProcess(new String[]{"sh","-c",cmd},null,null);int code=p.waitFor();runOnUiThread(()->log.setText(code==0?"Aplicado: "+cmd:"No se pudo ejecutar: "+cmd));}catch(Exception e){runOnUiThread(()->log.setText("Error: "+e.getMessage()));}}).start();}
}