package com.techfix.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context c){super(c,"techfix.db",null,2);}
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE branches(id INTEGER PRIMARY KEY,name TEXT,address TEXT,phone TEXT,lat REAL,lng REAL)");
        db.execSQL("CREATE TABLE services(id INTEGER PRIMARY KEY,name TEXT,category TEXT,price INTEGER)");
        db.execSQL("CREATE TABLE appointments(id INTEGER PRIMARY KEY AUTOINCREMENT,customer TEXT,device TEXT,service TEXT,branch TEXT,date TEXT,status TEXT,photo TEXT)");
        db.execSQL("CREATE TABLE history(id INTEGER PRIMARY KEY AUTOINCREMENT,device TEXT,service TEXT,branch TEXT,date TEXT,status TEXT)");
        db.execSQL("CREATE TABLE technicians(id INTEGER PRIMARY KEY,name TEXT,branch TEXT,available INTEGER)");
        db.execSQL("CREATE TABLE spare_parts(id INTEGER PRIMARY KEY,name TEXT,branch TEXT,quantity INTEGER)");
        db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,appointment_id INTEGER,amount INTEGER,status TEXT)");
        seed(db);
    }
    void seed(SQLiteDatabase db){
        db.execSQL("INSERT INTO branches VALUES(1,'TechFix Colombo','Colombo 03','0112345678',6.8921,79.8550)");
        db.execSQL("INSERT INTO branches VALUES(2,'TechFix Galle','Hirimbura Road, Galle','0912345678',6.0329,80.2168)");
        db.execSQL("INSERT INTO services VALUES(1,'Screen Replacement','Mobile',8500)");
        db.execSQL("INSERT INTO services VALUES(2,'Battery Replacement','Mobile',5500)");
        db.execSQL("INSERT INTO services VALUES(3,'Laptop Repair','Computer',7500)");
        db.execSQL("INSERT INTO services VALUES(4,'Windows Installation','Computer',3500)");
        db.execSQL("INSERT INTO services VALUES(5,'Virus & Malware Cleanup','Computer',4000)");
        db.execSQL("INSERT INTO technicians VALUES(1,'Kasun Perera','TechFix Colombo',1)");
        db.execSQL("INSERT INTO technicians VALUES(2,'Nimal Fernando','TechFix Galle',1)");
        db.execSQL("INSERT INTO technicians VALUES(3,'Ravindu Silva','TechFix Galle',0)");
        db.execSQL("INSERT INTO spare_parts VALUES(1,'iPhone Display','TechFix Colombo',8)");
        db.execSQL("INSERT INTO spare_parts VALUES(2,'Phone Battery','TechFix Colombo',12)");
        db.execSQL("INSERT INTO spare_parts VALUES(3,'Laptop SSD','TechFix Galle',5)");
        db.execSQL("INSERT INTO spare_parts VALUES(4,'Universal Charger','TechFix Galle',9)");
    }
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){db.execSQL("DROP TABLE IF EXISTS branches");db.execSQL("DROP TABLE IF EXISTS services");db.execSQL("DROP TABLE IF EXISTS appointments");db.execSQL("DROP TABLE IF EXISTS history");db.execSQL("DROP TABLE IF EXISTS technicians");db.execSQL("DROP TABLE IF EXISTS spare_parts");db.execSQL("DROP TABLE IF EXISTS payments");onCreate(db);}
    public Cursor branches(){return getReadableDatabase().rawQuery("SELECT * FROM branches",null);}
    public Cursor services(){return getReadableDatabase().rawQuery("SELECT * FROM services",null);}
    public long appointment(String customer,String device,String service,String branch,String date,String photo){
        SQLiteDatabase d=getWritableDatabase();ContentValues v=new ContentValues();v.put("customer",customer);v.put("device",device);v.put("service",service);v.put("branch",branch);v.put("date",date);v.put("status","Received");v.put("photo",photo);long id=d.insert("appointments",null,v);
        ContentValues h=new ContentValues();h.put("device",device);h.put("service",service);h.put("branch",branch);h.put("date",date);h.put("status","Received");d.insert("history",null,h);ContentValues p=new ContentValues();p.put("appointment_id",id);p.put("amount",0);p.put("status","Pending");d.insert("payments",null,p);return id;
    }
    public Cursor appointments(){return getReadableDatabase().rawQuery("SELECT * FROM appointments ORDER BY id DESC",null);}
    public Cursor history(){return getReadableDatabase().rawQuery("SELECT * FROM history ORDER BY id DESC",null);}
    public Cursor technicians(){return getReadableDatabase().rawQuery("SELECT * FROM technicians ORDER BY branch,name",null);}
    public Cursor spareParts(){return getReadableDatabase().rawQuery("SELECT * FROM spare_parts ORDER BY branch,name",null);}
    public void updateAppointmentStatus(int id,String status){SQLiteDatabase d=getWritableDatabase();ContentValues v=new ContentValues();v.put("status",status);d.update("appointments",v,"id=?",new String[]{String.valueOf(id)});ContentValues h=new ContentValues();h.put("status",status);d.update("history",h,"id=(SELECT id FROM history ORDER BY id DESC LIMIT 1)",null);}
}