package com.techfix.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context c){super(c,"techfix.db",null,1);}
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE branches(id INTEGER PRIMARY KEY,name TEXT,address TEXT,phone TEXT,lat REAL,lng REAL)");
        db.execSQL("CREATE TABLE services(id INTEGER PRIMARY KEY,name TEXT,category TEXT,price INTEGER)");
        db.execSQL("CREATE TABLE appointments(id INTEGER PRIMARY KEY AUTOINCREMENT,customer TEXT,device TEXT,service TEXT,branch TEXT,date TEXT,status TEXT,photo TEXT)");
        db.execSQL("CREATE TABLE history(id INTEGER PRIMARY KEY AUTOINCREMENT,device TEXT,service TEXT,branch TEXT,date TEXT,status TEXT)");
        db.execSQL("INSERT INTO branches VALUES(1,'TechFix Colombo','Colombo 03','0112345678',6.8921,79.8550)");
        db.execSQL("INSERT INTO branches VALUES(2,'TechFix Galle','Hirimbura Road, Galle','0912345678',6.0329,80.2168)");
        db.execSQL("INSERT INTO services VALUES(1,'Screen Replacement','Mobile',8500)");
        db.execSQL("INSERT INTO services VALUES(2,'Battery Replacement','Mobile',5500)");
        db.execSQL("INSERT INTO services VALUES(3,'Laptop Repair','Computer',7500)");
        db.execSQL("INSERT INTO services VALUES(4,'Windows Installation','Computer',3500)");
        db.execSQL("INSERT INTO services VALUES(5,'Virus & Malware Cleanup','Computer',4000)");
    }
    public void onUpgrade(SQLiteDatabase db,int o,int n){db.execSQL("DROP TABLE IF EXISTS branches");db.execSQL("DROP TABLE IF EXISTS services");db.execSQL("DROP TABLE IF EXISTS appointments");db.execSQL("DROP TABLE IF EXISTS history");onCreate(db);}
    public Cursor branches(){return getReadableDatabase().rawQuery("SELECT * FROM branches",null);}
    public Cursor services(){return getReadableDatabase().rawQuery("SELECT * FROM services",null);}
    public long appointment(String customer,String device,String service,String branch,String date,String photo){
        ContentValues v=new ContentValues(); v.put("customer",customer);v.put("device",device);v.put("service",service);v.put("branch",branch);v.put("date",date);v.put("status","Received");v.put("photo",photo);
        long id=getWritableDatabase().insert("appointments",null,v);
        ContentValues h=new ContentValues();h.put("device",device);h.put("service",service);h.put("branch",branch);h.put("date",date);h.put("status","Received");getWritableDatabase().insert("history",null,h);
        return id;
    }
    public Cursor appointments(){return getReadableDatabase().rawQuery("SELECT * FROM appointments ORDER BY id DESC",null);}
    public Cursor history(){return getReadableDatabase().rawQuery("SELECT * FROM history ORDER BY id DESC",null);}
}
