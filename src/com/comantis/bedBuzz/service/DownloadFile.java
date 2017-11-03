package com.comantis.bedBuzz.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadFile extends AsyncTask<Integer, Integer, Void>{
   
    public static final String FILE_DOWNLOADED = "com.comantis.bedBuzz.fileLoaded";
    
	SharedPreferences settings;
    SharedPreferences.Editor editor;
    String fileName;
    String url;
    Context context;
    
    public  DownloadFile(String fileName, String url, Context context){
        this.url = url;
        this.fileName = fileName;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        //This is where we would do the actual download stuff
        //for now I'm just going to loop for 10 seconds
        // publishing progress every second

        int count;

        try {


         URL url = new URL(this.url);
        URLConnection conexion = url.openConnection();
        conexion.connect();

        int lenghtOfFile = conexion.getContentLength();
        Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

        InputStream input = new BufferedInputStream(url.openStream());
        
     // create a File object for the parent directory
        File outputDirectory = new File(Environment.getExternalStorageDirectory() +"/bedBuzz/");
        // have the object build the directory structure, if needed.
        outputDirectory.mkdirs();
        
        // create nomdeia file, if it doesnt exist (to stop sounds being added to user's 'media'
        File nomediaFile = new File(outputDirectory, ".nomedia");
        if (!nomediaFile.exists())
        {
        	nomediaFile.createNewFile();
        }
        
        // create a File object for the output file
        File outputFile = new File(outputDirectory, this.fileName);
        
        OutputStream output = new FileOutputStream(outputFile);
        
        byte data[] = new byte[1024];

        long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                //publishProgress(""+(int)((total*100)/lenghtOfFile));
                Log.d("%Percentage%",""+(int)((total*100)/lenghtOfFile));
                onProgressUpdate((int)((total*100)/lenghtOfFile));
                output.write(data, 0, count);
            }
            
            output.flush();
            output.close();
            input.close();

        } catch (Exception e) 
        {
        	 Log.d("Error","Error "+e.getMessage());
        }


        return null;
    }
    protected void onPostExecute(Void result)    {
        // complete
    	Intent i = new Intent(FILE_DOWNLOADED);
        i.putExtra("filename",  this.fileName);
        context.sendBroadcast(i);
    }
}
