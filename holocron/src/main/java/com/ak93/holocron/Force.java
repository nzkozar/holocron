package com.ak93.holocron;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The Force is required to unlock the Holocrons and access the data inside them
 */
public class Force {
    private final String HOLOCRON_SHARED_PREF_NAME = "JediKnight"; //TODO replace with some random string
    private final String HOLOCRON_AES_PASS_PREF_KEY = "happk";
    private final String HOLOCRON_AES_IV_STRING_PREF_KEY = "haispk";
    private final String HOLOCRON_AES_SALT_STRING_PREF_KEY = "hasspk";

    private Context mContext;
    private SharedPreferences mPreferences;

    //Encryption fields
    private SecretKey mAESkey;
    private IvParameterSpec ivParams;

    private static String TAG = "Force";

    /**
     * Creates a Force object ready for use in encryption/decryption
     * This constructor will take some time to initialise.
     * @param context A context used to access SharedPreferences
     */
    public Force(Context context){
        Log.i(TAG,"Initializing Force...");
        mContext = context;
        mPreferences = mContext.getSharedPreferences(HOLOCRON_SHARED_PREF_NAME,Context.MODE_PRIVATE);
        String passPhrase;
        if(mPreferences.contains(HOLOCRON_AES_PASS_PREF_KEY)){
            passPhrase = mPreferences.getString(HOLOCRON_AES_PASS_PREF_KEY,null);
        }else{
            passPhrase = SHA256(Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID)); //save curent device id as SHA256 hash
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(HOLOCRON_AES_PASS_PREF_KEY,passPhrase);
            editor.apply();
        }

        int iterationCount = 10000;
        int keyLength = 256;
        int saltLength = keyLength / 8; // same size as key output

        byte[] mAESiv;
        byte[] mAESsalt;
        if(mPreferences.contains(HOLOCRON_AES_IV_STRING_PREF_KEY)
                && mPreferences.contains(HOLOCRON_AES_SALT_STRING_PREF_KEY)) { //Stored AES credentials

            mAESiv = Base64.decode(mPreferences.getString(HOLOCRON_AES_IV_STRING_PREF_KEY,""),Base64.NO_WRAP);
            mAESsalt = Base64.decode(mPreferences.getString(HOLOCRON_AES_SALT_STRING_PREF_KEY,"").getBytes(),Base64.NO_WRAP);
            ivParams = new IvParameterSpec(mAESiv);

            //Derive key
            try {
                KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), mAESsalt,
                        iterationCount, keyLength);
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
                mAESkey = new SecretKeySpec(keyBytes, "AES");

            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }else{ //This is the first time this method has been run and no AES params have yet been generated.
            try {
                SecureRandom random = new SecureRandom();
                mAESsalt = new byte[saltLength];
                random.nextBytes(mAESsalt);

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                mAESiv = new byte[cipher.getBlockSize()];
                random.nextBytes(mAESiv);
                ivParams = new IvParameterSpec(mAESiv);

                KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), mAESsalt,
                        iterationCount, keyLength);
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
                mAESkey = new SecretKeySpec(keyBytes, "AES");

                //Save iv & salt for later usage
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(HOLOCRON_AES_IV_STRING_PREF_KEY,Base64.encodeToString(mAESiv,Base64.NO_WRAP));
                editor.putString(HOLOCRON_AES_SALT_STRING_PREF_KEY,Base64.encodeToString(mAESsalt,Base64.NO_WRAP));
                editor.apply();

            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG,"Force init complete.");
    }

    /**
     * Encrypts a string using AES
     * @param data String to encrypt
     * @return Encrypted String
     */
    public String encrypt(String data){
        byte[] bytes = new byte[0];
        try {
            bytes = encrypt(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(bytes,Base64.NO_WRAP);
    }

    /**
     * Encrypts a byte array using AES
     * @param data byte array to encrypt
     * @return Encrypted byte array
     */
    public byte[] encrypt(byte[] data){
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, mAESkey, ivParams);

            return cipher.doFinal(data);
        }catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypts a AES encrypted string, that was previously encrypted using @encrypt method of this Class.
     * @param data String to decrypt
     * @return A decrypted string
     */
    public String decrypt(String data){
        byte[] plaintext = decrypt(Base64.decode(data,Base64.NO_WRAP));
        try {
            return new String(plaintext,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Decrypts a AES encrypted byte array, that was previously encrypted using @encrypt method of this Class.
     * @param data byte array to decrypt
     * @return A decrypted byte array
     */
    public byte[] decrypt(byte[] data){
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, mAESkey, ivParams);

            return cipher.doFinal(data);
        }catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a SHA256 hash based on the given String
     * @param in A string to hash
     * @return An SHA256 hashed string
     */
    private String SHA256(String in){
        String hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] bytes = digest.digest(in.getBytes());
            hash = String.format("%0" + (bytes.length*2) + "X", new BigInteger(1, bytes));
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return hash;
    }
}
