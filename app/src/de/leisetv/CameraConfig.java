package de.leisetv;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.AtomicFile;
import java.io.*;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONObject;

/** Stream credentials stay in app-private storage encrypted by Android Keystore. */
final class CameraConfig {
    private static final String KEY="leise-tv-camera-v1";
    private static javax.crypto.SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
        if(!ks.containsAlias(KEY)){
            KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
            g.init(new KeyGenParameterSpec.Builder(KEY,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
            g.generateKey();
        }
        return ((KeyStore.SecretKeyEntry)ks.getEntry(KEY,null)).getSecretKey();
    }
    static void save(Context c,JSONObject config) throws Exception {
        saveNamed(c,"camera",config);
    }
    static void saveNamed(Context c,String name,JSONObject config) throws Exception {
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());
        byte[] encrypted=cipher.doFinal(config.toString().getBytes("UTF-8"));
        AtomicFile file=new AtomicFile(new File(c.getFilesDir(),name+".enc"));
        FileOutputStream out=null;
        try{
            out=file.startWrite();out.write(cipher.getIV().length);out.write(cipher.getIV());out.write(encrypted);file.finishWrite(out);
        }catch(Exception e){if(out!=null)file.failWrite(out);throw e;}
    }
    static JSONObject read(Context c) throws Exception {
        return readNamed(c,"camera");
    }
    static JSONObject readNamed(Context c,String name) throws Exception {
        AtomicFile file=new AtomicFile(new File(c.getFilesDir(),name+".enc"));
        byte[] bytes=file.readFully();
        int n=bytes[0]&255;
        if(n!=12||bytes.length<n+18)throw new IOException("Invalid configuration");
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,bytes,1,n));
        return new JSONObject(new String(cipher.doFinal(bytes,1+n,bytes.length-1-n),"UTF-8"));
    }
}
