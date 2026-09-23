package com.techfix.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
    private LinearLayout root, content, nav;
    private TextView pageTitle, pageSubtitle;
    private DatabaseHelper db;
    private Uri photoUri;
    private ImageView photoPreview;
    private final int BLUE=Color.rgb(23,105,224), NAVY=Color.rgb(16,24,40), BG=Color.rgb(246,248,252), TEXT=Color.rgb(28,36,52), MUTED=Color.rgb(102,112,133), GREEN=Color.rgb(18,183,106), ORANGE=Color.rgb(247,144,9);

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        db=new DatabaseHelper(this);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        build();
    }

    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    TextView text(String s,float sp,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setIncludeFontPadding(false);return t;}
    TextView heading(String s){TextView t=text(s,24,TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    TextView label(String s){TextView t=text(s,13,MUTED);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    void margin(View v,int l,int top,int r,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(l),dp(top),dp(r),dp(bottom));v.setLayoutParams(p);}
    MaterialCardView card(){MaterialCardView c=new MaterialCardView(this);c.setRadius(dp(18));c.setCardElevation(dp(1));c.setStrokeWidth(dp(1));c.setStrokeColor(Color.rgb(226,231,239));c.setCardBackgroundColor(Color.WHITE);return c;}
    MaterialButton button(String s,boolean filled){MaterialButton b=new MaterialButton(this);b.setText(s);b.setTextSize(14);b.setAllCaps(false);b.setCornerRadius(dp(12));b.setMinHeight(dp(48));b.setPadding(dp(16),0,dp(16),0);b.setRippleColor(android.content.res.ColorStateList.valueOf(Color.rgb(220,232,255)));if(filled){b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLUE));b.setTextColor(Color.WHITE);}else{b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));b.setTextColor(BLUE);b.setStrokeColor(android.content.res.ColorStateList.valueOf(BLUE));b.setStrokeWidth(dp(1));}return b;}
    TextInputEditText input(String hint){TextInputLayout l=new TextInputLayout(this);l.setHint(hint);l.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);l.setBoxCornerRadii(dp(12),dp(12),dp(12),dp(12));TextInputEditText e=new TextInputEditText(this);e.setSingleLine(true);l.addView(e,new TextInputLayout.LayoutParams(-1,-2));content.addView(l);margin(l,0,0,0,12);return e;}

    void build(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.VERTICAL);top.setPadding(dp(20),dp(18),dp(20),dp(16));top.setBackgroundColor(NAVY);
        pageTitle=text("TechFix",24,Color.WHITE);pageTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);top.addView(pageTitle);
        pageSubtitle=text("Computer & Mobile Repair",13,Color.rgb(205,214,230));margin(pageSubtitle,0,5,0,0);top.addView(pageSubtitle);
        root.addView(top);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(18),dp(18),dp(18),dp(24));scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(dp(6),dp(7),dp(6),dp(7));nav.setBackgroundColor(Color.WHITE);
        String[] names={"Home","Services","Book","Track","History"};
        for(String n:names){TextView b=text(n,11,MUTED);b.setGravity(Gravity.CENTER);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setPadding(2,dp(9),2,dp(9));b.setOnClickListener(v->show(n));nav.addView(b,new LinearLayout.LayoutParams(0,dp(50),1));}
        root.addView(nav);
        setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{final int statusBarTop=Math.max(insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,dp(8));final int navBarBottom=insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;root.setPadding(0,statusBarTop,0,0);nav.setPadding(dp(6),dp(7),dp(6),Math.max(dp(7),navBarBottom));return insets;});
        ViewCompat.requestApplyInsets(root);
        show("Home");
    }

    void show(String page){
        content.removeAllViews();
        pageTitle.setText("TechFix");
        pageSubtitle.setText(page.equals("Home")?"Computer & Mobile Repair":page);
        switch(page){case "Home": home(); break; case "Services": services(); break; case "Book": book(); break; case "Track": track(); break; case "Manage": manage(); break; case "History": history(); break; case "Map": map(); break; default: home(); break;}
    }

    void heroImage(String url,int height){
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setBackgroundColor(Color.rgb(225,232,242));content.addView(img,new LinearLayout.LayoutParams(-1,dp(height)));margin(img,0,0,0,18);ImageLoader.load(url,img);
    }

    void home(){
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(20),dp(22),dp(20),dp(20));
        GradientDrawable heroBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(10,28,58),Color.rgb(23,105,224)});heroBg.setCornerRadius(dp(24));hero.setBackground(heroBg);content.addView(hero);margin(hero,0,0,0,16);
        TextView badge=text("TECHFIX  •  SERVICE CENTER",11,Color.rgb(190,220,255));badge.setTypeface(Typeface.DEFAULT,Typeface.BOLD);hero.addView(badge);
        TextView title=text("Your device. Back in action.",30,Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);margin(title,0,10,0,8);hero.addView(title);
        TextView sub=text("Book repairs, track progress and find a nearby branch from one clean workspace.",14,Color.rgb(225,235,250));sub.setLineSpacing(0,1.12f);hero.addView(sub);
        MaterialButton heroBook=button("Book a repair",true);heroBook.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));heroBook.setTextColor(NAVY);hero.addView(heroBook);margin(heroBook,dp(0),16,0,0);heroBook.setOnClickListener(v->show("Book"));

        LinearLayout stats=new LinearLayout(this);stats.setGravity(Gravity.CENTER);content.addView(stats);margin(stats,0,0,0,16);
        String[] statTitles={"2","5","24/7"};String[] statLabels={"Branches","Services","Request access"};for(int i=0;i<3;i++){MaterialCardView sc=card();LinearLayout sb=new LinearLayout(this);sb.setOrientation(LinearLayout.VERTICAL);sb.setPadding(dp(12),dp(13),dp(12),dp(13));TextView num=text(statTitles[i],20,BLUE);num.setTypeface(Typeface.DEFAULT,Typeface.BOLD);sb.addView(num);TextView lab=text(statLabels[i],11,MUTED);margin(lab,0,3,0,0);sb.addView(lab);sc.addView(sb);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,-2,1);sp.setMargins(i==0?0:dp(5),0,i==2?0:dp(5),0);stats.addView(sc,sp);}

        MaterialCardView status=card();LinearLayout statusBox=new LinearLayout(this);statusBox.setOrientation(LinearLayout.HORIZONTAL);statusBox.setGravity(Gravity.CENTER_VERTICAL);statusBox.setPadding(dp(16),dp(14),dp(16),dp(14));
        TextView dot=text("●",18,GREEN);statusBox.addView(dot);LinearLayout statusText=new LinearLayout(this);statusText.setOrientation(LinearLayout.VERTICAL);TextView live=text("Service center online",15,TEXT);live.setTypeface(Typeface.DEFAULT,Typeface.BOLD);statusText.addView(live);TextView liveSub=text("Requests are ready to be submitted",12,MUTED);margin(liveSub,0,3,0,0);statusText.addView(liveSub);statusBox.addView(statusText,new LinearLayout.LayoutParams(0,-2,1));TextView ready=text("READY",11,GREEN);ready.setTypeface(Typeface.DEFAULT,Typeface.BOLD);statusBox.addView(ready);status.addView(statusBox);content.addView(status);margin(status,0,0,0,16);

        TextView qh=heading("Quick actions");content.addView(qh);margin(qh,0,0,0,10);
        MaterialCardView actions=card();LinearLayout actionBox=new LinearLayout(this);actionBox.setOrientation(LinearLayout.VERTICAL);actionBox.setPadding(dp(14),dp(8),dp(14),dp(8));
        MaterialButton locate=button("Find nearest branch",false);actionBox.addView(locate);margin(locate,0,4,0,0);locate.setOnClickListener(v->nearestBranch());
        MaterialButton map=button("Open live branch map",false);actionBox.addView(map);margin(map,0,4,0,0);map.setOnClickListener(v->show("Map"));
        MaterialButton historyBtn=button("View repair history",false);actionBox.addView(historyBtn);historyBtn.setOnClickListener(v->show("History"));actions.addView(actionBox);content.addView(actions);margin(actions,0,0,0,16);

        TextView bh=heading("Our branches");content.addView(bh);margin(bh,0,0,0,10);
        Cursor c=db.branches();while(c.moveToNext()){MaterialCardView branchCard=card();LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(14),dp(16),dp(14));TextView n=text(c.getString(1),16,TEXT);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(n);TextView a=text(c.getString(2),13,MUTED);margin(a,4,5,0,0);box.addView(a);TextView phone=text(c.getString(3),12,BLUE);box.addView(phone);branchCard.addView(box);content.addView(branchCard);margin(branchCard,0,0,0,8);}c.close();

        MaterialCardView web=card();LinearLayout wb=new LinearLayout(this);wb.setOrientation(LinearLayout.VERTICAL);wb.setPadding(dp(16),dp(15),dp(16),dp(15));wb.addView(label("CONNECTED SERVICES"));TextView webTitle=text("OpenStreetMap + remote lookup",15,TEXT);webTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);margin(webTitle,5,0,0,4);wb.addView(webTitle);wb.addView(text("Real web requests are used for location data.",12,MUTED));MaterialButton check=button("Test remote service",false);wb.addView(check);margin(check,10,0,0,0);check.setOnClickListener(v->remoteData());web.addView(wb);content.addView(web);margin(web,0,6,0,0);
    }

    void map(){
        TextView h=heading("TechFix branch map");content.addView(h);margin(h,0,0,0,6);
        TextView p=text("Live OpenStreetMap view with TechFix branch locations.",14,MUTED);content.addView(p);margin(p,0,0,0,14);
        WebView mapView=new WebView(this);
        mapView.setWebViewClient(new WebViewClient());
        mapView.getSettings().setJavaScriptEnabled(true);
        mapView.getSettings().setDomStorageEnabled(true);
        mapView.setBackgroundColor(Color.rgb(232,237,244));
        content.addView(mapView,new LinearLayout.LayoutParams(-1,dp(360)));
        margin(mapView,0,0,0,14);

        String html =
                "<!doctype html>"
                + "<html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'>"
                + "<style>html,body,#map{height:100%;margin:0}body{font-family:Arial;background:#eef2f7}</style>"
                + "</head><body>"
                + "<div id='map'></div>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<script>"
                + "var map=L.map('map').setView([6.45,80.05],9);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'OpenStreetMap contributors'}).addTo(map);"
                + "var places=[['TechFix Colombo',6.8921,79.8550,'Colombo 03'],['TechFix Galle',6.0329,80.2168,'Hirimbura Road, Galle']];"
                + "places.forEach(function(x){"
                + "L.marker([x[1],x[2]]).addTo(map).bindPopup('<b>'+x[0]+'</b><br>'+x[3]);"
                + "});"
                + "</script></body></html>";

        mapView.loadDataWithBaseURL("https://openstreetmap.org/",html,"text/html","UTF-8",null);
        MaterialButton directions=button("Open Galle branch directions",true);content.addView(directions);margin(directions,0,0,0,10);
        directions.setOnClickListener(v->openDirections(6.0329,80.2168,"TechFix Galle"));
        MaterialButton locate=button("Find nearest branch",false);content.addView(locate);margin(locate,0,0,0,0);
        locate.setOnClickListener(v->nearestBranch());
    }

    void openDirections(double lat,double lng,String name){
        Uri uri=Uri.parse("geo:"+lat+","+lng+"?q="+lat+","+lng+"("+Uri.encode(name)+")");
        Intent intent=new Intent(Intent.ACTION_VIEW,uri);
        try{startActivity(intent);}catch(ActivityNotFoundException e){Toast.makeText(this,"No map app is installed",Toast.LENGTH_SHORT).show();}
    }

    void services(){
        TextView h=heading("Repair services");content.addView(h);margin(h,0,0,0,6);
        TextView p=text("Clear service categories and estimated prices.",14,MUTED);content.addView(p);margin(p,0,0,0,16);
        Cursor c=db.services();while(c.moveToNext()){
            MaterialCardView card=card();LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.HORIZONTAL);box.setGravity(Gravity.CENTER_VERTICAL);box.setPadding(dp(16),dp(15),dp(16),dp(15));
            LinearLayout left=new LinearLayout(this);left.setOrientation(LinearLayout.VERTICAL);left.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));TextView n=text(c.getString(1),16,TEXT);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);left.addView(n);TextView cat=text(c.getString(2),13,MUTED);margin(cat,0,5,0,0);left.addView(cat);box.addView(left);
            TextView price=text("LKR "+String.format(Locale.US,"%,d",c.getInt(3)),15,BLUE);price.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(price);card.addView(box);content.addView(card);margin(card,0,0,0,10);
        }c.close();
        MaterialButton b=button("Book a service",true);content.addView(b);margin(b,0,8,0,0);b.setOnClickListener(v->show("Book"));
    }

    void book(){
        TextView h=heading("Book a repair");content.addView(h);margin(h,0,0,0,6);
        TextView p=text("Tell us about the device. Your request is stored locally for offline coursework demonstration.",14,MUTED);content.addView(p);margin(p,0,0,0,18);
        TextInputEditText name=input("Customer name");
        TextInputEditText device=input("Device, e.g. iPhone 15 or Dell laptop");
        TextView sl=label("Repair service");content.addView(sl);margin(sl,0,2,0,5);
        Spinner service=new Spinner(this);ArrayList<String> ss=new ArrayList<>();Cursor c=db.services();while(c.moveToNext())ss.add(c.getString(1));c.close();service.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,ss));content.addView(service);margin(service,0,0,0,14);
        TextView bl=label("Branch");content.addView(bl);margin(bl,0,2,0,5);
        Spinner branch=new Spinner(this);ArrayList<String> bs=new ArrayList<>();c=db.branches();while(c.moveToNext())bs.add(c.getString(1));c.close();branch.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,bs));content.addView(branch);margin(branch,0,0,0,14);
        TextInputEditText date=input("Preferred repair date");
        date.setFocusable(false);date.setOnClickListener(v->pickDate(date));
        TextView photoLabel=label("DEVICE PHOTO");content.addView(photoLabel);margin(photoLabel,0,2,0,8);
        MaterialButton photo=button("Take a photo",false);content.addView(photo);margin(photo,0,8,0,8);photo.setOnClickListener(v->camera());
        photoPreview=new ImageView(this);photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);photoPreview.setVisibility(View.GONE);content.addView(photoPreview,new LinearLayout.LayoutParams(-1,dp(180)));margin(photoPreview,0,0,0,14);
        MaterialButton submit=button("Submit repair request",true);content.addView(submit);margin(submit,0,0,0,12);
        submit.setOnClickListener(v->{String n=name.getText()==null?"":name.getText().toString().trim();String d=device.getText()==null?"":device.getText().toString().trim();if(n.isEmpty()||d.isEmpty()){Toast.makeText(this,"Enter your name and device details",Toast.LENGTH_SHORT).show();return;}String dt=date.getText()==null?"":date.getText().toString();long id=db.appointment(n,d,service.getSelectedItem().toString(),branch.getSelectedItem().toString(),dt,photoUri==null?"":photoUri.toString());new AlertDialog.Builder(this).setTitle("Request received").setMessage("Repair request #"+id+" has been saved.\nStatus: Received").setPositiveButton("Track request",(x,w)->show("Track")).setNegativeButton("Close",null).show();});
    }

    void pickDate(EditText e){
        Calendar now=Calendar.getInstance();DatePickerDialog d=new DatePickerDialog(this,(v,y,m,day)->{Calendar x=Calendar.getInstance();x.set(y,m,day);e.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(x.getTime()));},now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH));d.show();
    }

    void track(){
        TextView h=heading("Repair tracking");content.addView(h);margin(h,0,0,0,6);TextView p=text("Follow the current status of every repair request.",14,MUTED);content.addView(p);margin(p,0,0,0,16);
        Cursor c=db.appointments();if(c.getCount()==0){TextView e=text("No repair requests yet.",15,MUTED);content.addView(e);}while(c.moveToNext()){
            MaterialCardView card=card();LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(15),dp(16),dp(15));
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView id=text("REQUEST #"+c.getInt(0),12,MUTED);id.setTypeface(Typeface.DEFAULT,Typeface.BOLD);row.addView(id,new LinearLayout.LayoutParams(0,-2,1));TextView status=text(c.getString(6),12,GREEN);status.setTypeface(Typeface.DEFAULT,Typeface.BOLD);row.addView(status);box.addView(row);
            TextView dev=text(c.getString(2)+"\n"+c.getString(3),16,TEXT);dev.setTypeface(Typeface.DEFAULT,Typeface.BOLD);margin(dev,0,10,0,0);box.addView(dev);
            TextView info=text(c.getString(4)+"\n"+(c.getString(5).isEmpty()?"Date not selected":c.getString(5)),13,MUTED);box.addView(info);card.addView(box);content.addView(card);margin(card,0,0,0,10);
        }c.close();
        MaterialCardView pay=card();LinearLayout pb=new LinearLayout(this);pb.setPadding(dp(16),dp(14),dp(16),dp(14));pb.setOrientation(LinearLayout.VERTICAL);pb.addView(label("PAYMENT"));pb.addView(text("Pending · demo payment flow",15,TEXT));pay.addView(pb);content.addView(pay);
    }

    void manage(){
        TextView h=heading("Management dashboard");content.addView(h);margin(h,0,0,0,6);TextView p=text("Coursework demo for appointments, technicians and spare-parts availability.",14,MUTED);content.addView(p);margin(p,0,0,0,16);
        Cursor a=db.appointments();int total=a.getCount();a.close();MaterialCardView summary=card();LinearLayout sb=new LinearLayout(this);sb.setPadding(dp(16),dp(16),dp(16),dp(16));sb.setOrientation(LinearLayout.HORIZONTAL);TextView n=text(String.valueOf(total),28,BLUE);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);sb.addView(n);TextView q=text("  total repair requests",14,MUTED);q.setGravity(Gravity.CENTER_VERTICAL);sb.addView(q);summary.addView(sb);content.addView(summary);margin(summary,0,0,0,14);
        TextView th=heading("Technicians");content.addView(th);margin(th,0,0,0,10);Cursor t=db.technicians();while(t.moveToNext()){MaterialCardView card=card();LinearLayout box=new LinearLayout(this);box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setOrientation(LinearLayout.VERTICAL);box.addView(text(t.getString(1),15,TEXT));box.addView(text(t.getString(2)+" · "+(t.getInt(3)==1?"Available":"Busy"),13,t.getInt(3)==1?GREEN:ORANGE));card.addView(box);content.addView(card);margin(card,0,0,0,8);}t.close();
        TextView ph=heading("Spare parts");content.addView(ph);margin(ph,8,10,0,10);Cursor s=db.spareParts();while(s.moveToNext()){MaterialCardView card=card();LinearLayout box=new LinearLayout(this);box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setOrientation(LinearLayout.HORIZONTAL);TextView left=text(s.getString(1)+"\n"+s.getString(2),14,TEXT);left.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));box.addView(left);box.addView(text("Qty "+s.getInt(3),14,BLUE));card.addView(box);content.addView(card);margin(card,0,0,0,8);}s.close();
        TextView ah=heading("Update repair status");content.addView(ah);margin(ah,8,10,0,8);Cursor ap=db.appointments();ArrayList<Integer> ids=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();while(ap.moveToNext()){ids.add(ap.getInt(0));labels.add("#"+ap.getInt(0)+" · "+ap.getString(2));}ap.close();if(ids.isEmpty()){content.addView(text("Create a repair request first.",14,MUTED));return;}Spinner req=new Spinner(this);req.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));content.addView(req);margin(req,0,0,0,10);Spinner st=new Spinner(this);st.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Received","Diagnosing","Repairing","Ready for Collection","Completed"}));content.addView(st);margin(st,0,0,0,10);MaterialButton update=button("Save status",true);content.addView(update);margin(update,0,0,0,10);update.setOnClickListener(v->{db.updateAppointmentStatus(ids.get(req.getSelectedItemPosition()),st.getSelectedItem().toString());Toast.makeText(this,"Status updated",Toast.LENGTH_SHORT).show();});
    }

    void history(){
        TextView h=heading("Repair history");content.addView(h);margin(h,0,0,0,6);TextView p=text("Your previous repair requests stored on this device.",14,MUTED);content.addView(p);margin(p,0,0,0,16);
        Cursor c=db.history();if(c.getCount()==0){TextView e=text("No repair history yet.",15,MUTED);content.addView(e);}while(c.moveToNext()){
            MaterialCardView card=card();LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(14),dp(16),dp(14));TextView d=text(c.getString(1),16,TEXT);d.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(d);TextView s=text(c.getString(2)+" · "+c.getString(3),13,MUTED);margin(s,0,6,0,0);box.addView(s);TextView st=text(c.getString(4)+" · "+c.getString(5),12,MUTED);box.addView(st);card.addView(box);content.addView(card);margin(card,0,0,0,10);
        }c.close();
    }

    void camera(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},20);return;}
        try{
            File dir=new File(getExternalFilesDir(null),"photos");if(!dir.exists()&&!dir.mkdirs()){Toast.makeText(this,"Photo folder could not be created",Toast.LENGTH_SHORT).show();return;}File file=new File(dir,"device_"+System.currentTimeMillis()+".jpg");photoUri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
            Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,21);
        }catch(Exception e){Toast.makeText(this,"Camera could not start",Toast.LENGTH_SHORT).show();}
    }

    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==21&&result==RESULT_OK&&photoUri!=null){if(photoPreview!=null){photoPreview.setVisibility(View.VISIBLE);photoPreview.setImageURI(photoUri);}Toast.makeText(this,"Device photo attached",Toast.LENGTH_SHORT).show();}}

    void nearestBranch(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},22);return;}
        LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location last=null;try{last=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(last==null)last=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);}catch(Exception ignored){}
        if(last==null){new AlertDialog.Builder(this).setTitle("Location unavailable").setMessage("Turn on location and try again. On the Pixel emulator, set a location in the emulator controls first.").setPositiveButton("OK",null).show();return;}
        double best=Double.MAX_VALUE;String bn="",ba="";Cursor c=db.branches();while(c.moveToNext()){double d=distance(last.getLatitude(),last.getLongitude(),c.getDouble(4),c.getDouble(5));if(d<best){best=d;bn=c.getString(1);ba=c.getString(2);}}c.close();
        new AlertDialog.Builder(this).setTitle("Nearest TechFix branch").setMessage(String.format(Locale.US,"%s\n%s\n\n%.1f km from your current location.",bn,ba,best)).setPositiveButton("OK",null).show();
    }

    double distance(double a,double b,double c,double d){double R=6371,la=Math.toRadians(c-a),lo=Math.toRadians(d-b);double x=Math.sin(la/2)*Math.sin(la/2)+Math.cos(Math.toRadians(a))*Math.cos(Math.toRadians(c))*Math.sin(lo/2)*Math.sin(lo/2);return R*2*Math.atan2(Math.sqrt(x),Math.sqrt(1-x));}

    void remoteData(){
        Toast.makeText(this,"Loading remote data...",Toast.LENGTH_SHORT).show();
        new Thread(()->{try{URL u=new URL("https://nominatim.openstreetmap.org/search?q=Galle%20Sri%20Lanka&format=json&limit=1");HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setConnectTimeout(8000);h.setReadTimeout(8000);h.setRequestProperty("User-Agent","TechFix-Student-App");BufferedReader r=new BufferedReader(new InputStreamReader(h.getInputStream()));StringBuilder s=new StringBuilder();String line;while((line=r.readLine())!=null)s.append(line);r.close();JSONArray a=new JSONArray(s.toString());String display=a.length()>0?a.getJSONObject(0).optString("display_name","Galle"):"No result";runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Remote web service").setMessage(display).setPositiveButton("OK",null).show());}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Remote service unavailable. Try again.",Toast.LENGTH_SHORT).show());}}).start();
    }
}