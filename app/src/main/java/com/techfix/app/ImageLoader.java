package com.techfix.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {
    private static final LruCache<String,Bitmap> cache=new LruCache<>(8);
    private static final ExecutorService pool=Executors.newFixedThreadPool(3);
    private ImageLoader(){}
    public static void load(String url,ImageView view){
        Bitmap b=cache.get(url);if(b!=null){view.setImageBitmap(b);return;}
        pool.execute(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(7000);c.setReadTimeout(7000);c.connect();InputStream in=c.getInputStream();Bitmap x=BitmapFactory.decodeStream(in);in.close();c.disconnect();if(x!=null){cache.put(url,x);view.post(()->view.setImageBitmap(x));}}catch(Exception ignored){}}); 
    }
}