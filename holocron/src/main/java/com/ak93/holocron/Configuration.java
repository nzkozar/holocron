package com.ak93.holocron;

import android.util.Log;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * This class contains all configuration info needed for Holocron to operate
 */
public class Configuration {

    private HashMap<String, String> classHashes;
    private HashMap<String,Long> classStoredIDs;

    public Configuration(){
        classHashes = new HashMap<>();
        classStoredIDs = new HashMap<>();
    }

    public void removeClass(Class c){
        classHashes.remove(c.getName());
        classStoredIDs.remove(c.getName());
    }

    public long getNextClassId(Class c){
        if(classStoredIDs.containsKey(c.getName())) return classStoredIDs.get(c.getName())+1;
        return 0;
    }

    public long getClassStoredID(Class c) {
        if(classStoredIDs.containsKey(c.getName())) return classStoredIDs.get(c.getName());
        return 0;
    }

    /**
     * Saves the highest id for a stored class.
     * @param c Stored class
     * @param id New id to compare to the old and stores it if needed
     * @return True if the stored id has changed and this configuration should be saved
     */
    public boolean setClassStoredID(Class c, long id){
        if(classStoredIDs.containsKey(c.getName())) {
            long storedId = classStoredIDs.get(c.getName());
            if (storedId < id){
                classStoredIDs.put(c.getName(), id);
                return true;
            }
            return false;
        }else{
            classStoredIDs.put(c.getName(), id);
            return true;
        }
    }

    /**
     * Retrieves a stored class hash to be used for object retrieval
     * @param c Class whose hash to retrieve
     * @return Sting of a stored Class hash or null if no hash is stored for this Class
     */
    public String getClassHash(Class c){
        return classHashes.get(c.getName());
    }

    /**
     * Adds a Class hash to the Configuration. Configuration should be saved after this method returns.
     * @param c The class to add to the hash list
     * @return A newly added hash
     */
    public String addClassHash(Class c){
        String classHash = hashClass(c);
        classHashes.put(c.getName(),classHash);
        return classHash;
    }

    /**
     * Hashes the class name using SHA-256 hash
     * @param c The class whose name to hash
     * @return A hashed class name as a String
     */
    private String hashClass(Class c){
        //SHA256
        String hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] bytes = digest.digest(c.getName().getBytes());
            hash = String.format("%0" + (bytes.length*2) + "X", new BigInteger(1, bytes));
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        Log.i("JediConfiguration","Class "+c.getName()+" hash: "+hash);
        return hash;
    }
}
