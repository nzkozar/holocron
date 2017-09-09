package com.ak93.holocron;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Holocron - Encrypted Object Storage for Android
 * "Holocrons are ancient repositories of knowledge and wisdom
 * that can only be accessed by those skilled in the Force."
 */
public class Holocron {

    public static final int HOLOCRON_RESPONSE_INITIALIZED = 500;
    public static final int HOLOCRON_RESPONSE_OBJECTS_RETRIEVED = 501;
    public static final int HOLOCRON_RESPONSE_OBJECTS_REMOVED = 502;

    private Context mContext;
    private Configuration mConfiguration;
    private Gson mGson;
    private boolean initialized = false;
    private boolean debug = false;

    //Encryption fields
    private volatile Force mForce;

    private final String TAG = "Holocron";

    /**
     * Standard constructor.
     * NOTE: Executes on main thread and will block for 2-3 seconds.
     * If this is not acceptable for your application, use the asynchronous constructor.
     * @param context Activity or application Context
     */
    public Holocron(Context context){
        mContext = context;
        mGson = new Gson();

        mForce = new Force(mContext);

        try{
            readConfiguration(); //Ensure we have configuration file
        }catch (OutOfMemoryError error){
            if(debug) error.printStackTrace();
        }
    }

    /**
     * Asynchronous constructor. Use this constructor to avoid having your main thread execution delayed.
     * @param context Activity or application Context
     * @param callback A response callback handler, that will receive a callback when Holocron
     *                 has been initialized. Null can be passed if no response is required.
     */
    public Holocron(final Context context, @Nullable final HolocronInitListener callback){
        mContext = context;
        mGson = new Gson();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mForce = new Force(context);
                try{
                    readConfiguration(); //Ensure we have configuration file
                    if(callback!=null) callback.onHolocronInitComplete();
                }catch (OutOfMemoryError error){
                    if(debug) error.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Enables debug logging
     * @param debug
     */
    public void enableDebug(boolean debug){
        this.debug = debug;
    }

    /**
     * Saves an object
     * @param o The object to be saved
     */
    public boolean put(Object o,long id){
        if(mConfiguration==null){
            try{
                readConfiguration(); //Ensure we have configuration file
            }catch (OutOfMemoryError error){
                return false;
            }
        }
        String classHash = mConfiguration.getClassHash(o.getClass());
        if(classHash==null){
            classHash = mConfiguration.addClassHash(o.getClass());
            writeConfiguration();
        }
        String filename = classHash + "_" + String.valueOf(id);
        if(mConfiguration.setClassStoredID(o.getClass(),id))writeConfiguration();
        return writeObject(filename,mGson.toJson(o));
    }

    /**
     * Retrieves all Objects of a particular class
     * @param c The class of the objects to retrieve
     * @return An ArrayList of all objects stored using the provided class
     */
    public <T>List<T> getAll(Class<T> c) throws OutOfMemoryError{
        List<T> objects = new ArrayList<>();
        if(mConfiguration==null){
            try{
                readConfiguration(); //Ensure we have configuration file
            }catch (OutOfMemoryError error){
                return objects;
            }
        }
        final String classHash = mConfiguration.getClassHash(c);
        if(classHash==null){
            return objects;
        }else {
            File filesDir = mContext.getFilesDir();
            File[] objectFiles = filesDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.contains(classHash);
                }
            });

            //sort object files by name
            //Bubble sort algorithm (sort by name) //TODO upgrade to a faster sort algorithm
            //Log.i(TAG,"Sort started");

            boolean sorted = false;
            while (!sorted) {
                boolean switched = false;
                for (int i = 0; i < (objectFiles.length - 1); i++) {
                    File a = objectFiles[i];
                    File b = objectFiles[i + 1];

                    Long idAlong = Long.parseLong(a.getName().split("_")[1]);
                    Long idBlong = Long.parseLong(b.getName().split("_")[1]);

                    if (idBlong < idAlong) {
                        objectFiles[i] = b;
                        objectFiles[i + 1] = a;
                        if (debug)
                            Log.i(TAG, "BUBLESORT@" + i + " SWITCHED: " + idAlong + " & " + idBlong);
                        switched = true;
                    }
                }
                if (!switched) {
                    sorted = true;
                }
            }
            //Log.i(TAG,"Sorted!");

            for (File f : objectFiles) {
                if (f.isFile()) {
                    String objectJson = readObject(f.getName());
                    if (objectJson != null)
                        objects.add(mGson.fromJson(objectJson, c));
                }
            }
            return objects;
        }
    }

    /**
     * Retrieves all Objects of a particular class and returns them using the provided callback.
     * All object reading work is done on a background thread.
     * @param c The class of the objects to retrieve
     * @param callback A callback through which the data is returned.
     */
    public <T> void getAllAsync(final Class<T> c, final HolocronResponseHandler<T> callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.onHolocronResponse(HOLOCRON_RESPONSE_OBJECTS_RETRIEVED,
                        new HolocronResponse<T>(null,getAll(c)));
            }
        }).start();
    }

    /**
     * Retrieves an object from storage.
     * @param c Class of the object to retrieve
     * @param id Id of the object to retrieve
     * @return An object of Class c and saved with id or null if no such object is stored.
     */
    public <T>T get(final Class<T> c,final long id){
        String filename = getObjectFileName(c,id);
        if(filename==null){
            //Log.e(TAG,"No class hash in configuration!!!");
            return null;
        }
        File objectFile = new File(mContext.getFilesDir().getAbsolutePath()+
                "/"+filename);
        if(!objectFile.exists()){
            //Log.e(TAG,"Object File doesn't exist!!!");
            return null;
        }


        return mGson.fromJson(readObject(filename),c);
    }

    /**
     * Removes a specific object from storage.
     * @param c Class of the stored object
     * @param id Id of the stored object
     * @return Returns true if the object has been successfully removed
     */
    public boolean remove(final Class c,final long id) {
        String filename = getObjectFileName(c, id);
        if (filename == null) {
            return false;
        }
        File objectFile = new File(mContext.getFilesDir().getAbsolutePath() +
                "/" + filename);
        return objectFile.exists() && objectFile.delete();

    }

    /**
     * Removes all saved objects matching the provided class
     * @param c Class to match to stored objects
     * @return true if all objects of c were removed
     */
    public boolean removeAll(Class c){
        if(mConfiguration==null){
            try {
                readConfiguration(); //Ensure we have configuration file
            }catch (OutOfMemoryError error){
                return false;
            }
        }
        final String classHash = mConfiguration.getClassHash(c);
        if(classHash==null)return true;
        File filesDir = mContext.getFilesDir();
        File[] objectFiles = filesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(classHash);
            }
        });

        for(File f:objectFiles){
            if(f.isFile()) {
                f.delete();
            }
        }
        mConfiguration.removeClass(c);
        writeConfiguration();
        return true;
    }

    public void removeAllAsync(final Class c, @Nullable final HolocronResponseHandler callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                removeAll(c);
                if(callback!=null){
                    callback.onHolocronResponse(HOLOCRON_RESPONSE_OBJECTS_REMOVED
                            ,new HolocronResponse<>(null,null,c));
                }
            }
        }).start();
    }

    @Nullable
    private String getObjectFileName(Class c, long id){
        if(mConfiguration==null){
            try{
                readConfiguration(); //Ensure we have configuration file
            }catch (OutOfMemoryError error){
                return null;
            }
        }
        String classHash = mConfiguration.getClassHash(c);
        if(classHash == null)return null;
        return classHash + "_" + String.valueOf(id);
    }

    /**
     * @return The current Configuration object of this Holocron instance
     */
    public Configuration getConfiguration(){
        return mConfiguration;
    }

    /**
     * Retrieves a saved Holocron configuration object
     */
    private void readConfiguration() throws OutOfMemoryError{
        File file = new File(mContext.getFilesDir().getAbsolutePath()+"/"+"cfg.j");
        String json;
        if(file.exists()) {
            json = readObject("cfg.j");
            if(json==null)throw new OutOfMemoryError();
            mConfiguration = mGson.fromJson(json, Configuration.class);
        }else{
            mConfiguration = new Configuration();
            writeConfiguration();
        }
        initialized = true;
    }

    private boolean writeConfiguration(){
        writeObject("cfg.j",mGson.toJson(mConfiguration));
        return true;
    }

    /**
     * Stores an object to a file. If a file for this object already exists, it will be overwritten.
     * @param filename Name to use for the file
     * @param data String data to write to this file
     * @return true if the write succeded
     */
    private boolean writeObject(String filename,String data){
        //Encrypt data with AES before writing it
        String encryptedData;
        try {
            if(mForce!=null){
                encryptedData = mForce.encrypt(data);
            }else{
                if(debug)Log.e(TAG,"writeObject mForce == null");
                return false;
            }
        }catch (OutOfMemoryError e){
            return false;
        }
        FileOutputStream os;
        try{
            os = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os);
            outputStreamWriter.write(encryptedData);
            outputStreamWriter.close();
            os.close();
            //Log.i(TAG,"Saved file with filename: "+filename+" & data: "+data);
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reads a file into a json string
     * @param filename
     * @return
     */
    private String readObject(String filename) throws OutOfMemoryError{
        //Log.i(TAG,"Reading file with filename: "+filename);
        String ret = null;
        try {
            InputStream inputStream = mContext.openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();
                while((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                if(mForce!=null) {
                    ret = stringBuilder.toString();

                    //Decrypt data to a JSON string
                    ret = mForce.decrypt(ret);
                }else {
                    if(debug)Log.e(TAG,"readObject mForce == null");
                }
            }
        } catch (FileNotFoundException e) {
            //Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            //Log.e(TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }

    public boolean isInitialized(){
        return initialized;
    }

    /**
     * @return The Force object used for encryption/decryption
     */
    private Force getForce() {
        return mForce;
    }

    /**
     * Encrypts a string using AES.
     * @param string Unencrypted string
     * @return An encrypted string.
     */
    public String encryptString(String string) throws IllegalStateException, OutOfMemoryError{
        if(!initialized)throw new IllegalStateException("Holocron is not initialized!");
        return mForce.encrypt(string);
    }

    /**
     * Encrypts a byte array using AES.
     * @param bytes Unencrypted byte array
     * @return An encrypted byte array.
     */
    public byte[] encryptBytes(byte[] bytes) throws IllegalStateException, OutOfMemoryError{
        if(!initialized)throw new IllegalStateException("Holocron is not initialized!");
        return mForce.encrypt(bytes);
    }

    /**
     * Decrypts a string using AES.
     * @param string Encrypted string, previously encrypted using @encryptString
     * @return A decrypted string.
     */
    public String decryptString(String string) throws IllegalStateException, OutOfMemoryError{
        if(!initialized)throw new IllegalStateException("Holocron is not initialized!");
        return mForce.decrypt(string);
    }

    /**
     * Decrypts a byte array using AES.
     * @param bytes Encrypted byte array, previously encrypted using @encryptBytes
     * @return A decrypted byte array.
     */
    public byte[] decryptBytes(byte[] bytes) throws IllegalStateException, OutOfMemoryError{
        if(!initialized)throw new IllegalStateException("Holocron is not initialized!");
        return mForce.decrypt(bytes);
    }
}