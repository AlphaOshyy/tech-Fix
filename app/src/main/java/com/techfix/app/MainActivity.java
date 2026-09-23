package com.techfix.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
    LinearLayout root,content;
    DatabaseHelper db;
    TextView title, gpsText;
    Uri photoUri;

    int blue=Color.rgb(23,105,224), navy=Color.rgb(16,24,40);

    @Override public void onCreate(Bundle b){
        super.onCreate(b); db=new DatabaseHelper(this); build();
    }

    TextView tv(String s,int sp){ TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(Color.DKGRAY);t.setPadding(16,12,16,12);return t; }
    Button btn(String s){ Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b; }

    void build(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(248,250,252));
        LinearLayout head=new LinearLayout(this);head.setOrientation(LinearLayout.VERTICAL);head.setPadding(20,22,20,18);head.setBackgroundColor(navy);
        title=tv("TechFix",26);title.setTextColor(Color.WHITE);head.addView(title);
        TextView sub=tv("Computer & Mobile Repair",14);sub.setTextColor(Color.LTGRAY);head.addView(sub);
        root.addView(head);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(16,16,16,16);
        ScrollView sv=new ScrollView(this);sv.addView(content);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this);nav.setPadding(6,4,6,6);nav.setBackgroundColor(Color.WHITE);
        String[] ns={"Home","Services","Book","Track","History"};
        for(String n:ns){Button x=btn(n);x.setOnClickListener(v->show(n));nav.addView(x,new LinearLayout.LayoutParams(0,56,1));}
        root.addView(nav);setContentView(root);show("Home");
    }

    void show(String page){
        content.removeAllViews();title.setText("TechFix  •  "+page);
        if(page.equals("Home")) home();
        else if(page.equals("Services")) services();
        else if(page.equals("Book")) book();
        else if(page.equals("Track")) track();
        else history();
    }

    void home(){
        content.addView(tv("Repair support made simple",24));
        content.addView(tv("Book a repair, locate your nearest branch, track your request, and keep your repair history.",16));
        Button gps=btn("Find nearest TechFix branch");content.addView(gps);gps.setOnClickListener(v->nearestBranch());
        gpsText=tv("GPS: waiting for location...",15);content.addView(gpsText);
        content.addView(tv("Branches",20));
        Cursor c=db.branches();while(c.moveToNext())content.addView(tv("• "+c.getString(1)+"\n"+c.getString(2)+"\n"+c.getString(3),15));c.close();
        Button cam=btn("Take device photo");content.addView(cam);cam.setOnClickListener(v->camera());
        Button remote=btn("Check free web service");content.addView(remote);remote.setOnClickListener(v->remoteData());
    }

    void services(){
        content.addView(tv("Repair services",24));
        Cursor c=db.services();while(c.moveToNext()){content.addView(tv(c.getString(1)+"  |  "+c.getString(2)+"\nEstimated price: LKR "+c.getInt(3),16));}c.close();
    }

    void book(){
        content.addView(tv("Book repair appointment",24));
        EditText name=new EditText(this);name.setHint("Customer name");content.addView(name);
        EditText device=new EditText(this);device.setHint("Device, e.g. iPhone 15 / Dell laptop");content.addView(device);
        Spinner service=new Spinner(this);ArrayList<String> ss=new ArrayList<>();Cursor c=db.services();while(c.moveToNext())ss.add(c.getString(1));c.close();service.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,ss));content.addView(service);
        Spinner branch=new Spinner(this);ArrayList<String> bs=new ArrayList<>();c=db.branches();while(c.moveToNext())bs.add(c.getString(1));c.close();branch.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,bs));content.addView(branch);
        EditText date=new EditText(this);date.setHint("Preferred date, e.g. 2026-09-25");content.addView(date);
        Button photo=btn("Attach device photo");content.addView(photo);photo.setOnClickListener(v->camera());
        Button submit=btn("Submit repair request");content.addView(submit);
        submit.setOnClickListener(v->{if(name.getText().toString().trim().isEmpty()||device.getText().toString().trim().isEmpty()){Toast.makeText(this,"Enter customer and device details",Toast.LENGTH_SHORT).show();return;}long id=db.appointment(name.getText().toString(),device.getText().toString(),service.getSelectedItem().toString(),branch.getSelectedItem().toString(),date.getText().toString(),photoUri==null?"":photoUri.toString());Toast.makeText(this,"Request #"+id+" received",Toast.LENGTH_LONG).show();show("Track");});
    }

    void track(){
        content.addView(tv("Repair tracking",24));
        Cursor c=db.appointments();if(c.getCount()==0)content.addView(tv("No repair requests yet.",16));
        while(c.moveToNext()){content.addView(tv("Request #"+c.getInt(0)+"\n"+c.getString(2)+" • "+c.getString(3)+"\nBranch: "+c.getString(4)+"\nDate: "+c.getString(5)+"\nStatus: "+c.getString(6),16));}c.close();
        content.addView(tv("Payment status: Pending. Payment is represented as a demo flow for coursework.",14));
    }

    void history(){
        content.addView(tv("Repair history",24));
        Cursor c=db.history();if(c.getCount()==0)content.addView(tv("No repair history yet.",16));
        while(c.moveToNext())content.addView(tv(c.getString(1)+" • "+c.getString(2)+"\n"+c.getString(3)+" | "+c.getString(4)+" | "+c.getString(5),16));c.close();
    }

    void camera(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},20);return;}
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);if(i.resolveActivity(getPackageManager())!=null)startActivityForResult(i,21);
    }
    @Override protected void onActivityResult(int r,int code,Intent data){super.onActivityResult(r,code,data);if(r==21&&data!=null){photoUri=data.getData();Toast.makeText(this,"Photo attached",Toast.LENGTH_SHORT).show();}}

    void nearestBranch(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},22);return;}
        LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);
        Location last=null;try{last=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(last==null)last=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);}catch(Exception e){}
        if(last==null){gpsText.setText("GPS unavailable. Try again outdoors.");return;}
        double best=Double.MAX_VALUE;String bn="";
        Cursor c=db.branches();while(c.moveToNext()){double d=distance(last.getLatitude(),last.getLongitude(),c.getDouble(4),c.getDouble(5));if(d<best){best=d;bn=c.getString(1);}}c.close();
        gpsText.setText(String.format(Locale.US,"Nearest: %s\nDistance: %.1f km\nYour GPS: %.4f, %.4f",bn,best,best==Double.MAX_VALUE?0:last.getLatitude(),last.getLongitude()));
    }
    double distance(double a,double b,double c,double d){double R=6371,la=Math.toRadians(c-a),lo=Math.toRadians(d-b);double x=Math.sin(la/2)*Math.sin(la/2)+Math.cos(Math.toRadians(a))*Math.cos(Math.toRadians(c))*Math.sin(lo/2)*Math.sin(lo/2);return R*2*Math.atan2(Math.sqrt(x),Math.sqrt(1-x));}

    void remoteData(){
        Toast.makeText(this,"Loading free Nominatim web service...",Toast.LENGTH_SHORT).show();
        new Thread(()->{try{URL u=new URL("https://nominatim.openstreetmap.org/search?q=Galle%20Sri%20Lanka&format=json&limit=1");HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setRequestProperty("User-Agent","TechFix-Student-App");BufferedReader r=new BufferedReader(new InputStreamReader(h.getInputStream()));StringBuilder s=new StringBuilder();String line;while((line=r.readLine())!=null)s.append(line);r.close();JSONArray a=new JSONArray(s.toString());String display=a.length()>0?a.getJSONObject(0).optString("display_name","Galle"):"No result";runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Remote data").setMessage("OpenStreetMap Nominatim result:\n"+display).setPositiveButton("OK",null).show());}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Web service unavailable",Toast.LENGTH_SHORT).show());}}).start();
    }
}
