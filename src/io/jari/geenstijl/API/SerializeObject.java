package io.jari.geenstijl.API;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.util.Base64InputStream;
import android.util.Base64OutputStream;

/**
 * Take an object and serialize and then save it to preferences
 * @author John Matthews
 *
 */
public class SerializeObject {
    private final static String TAG = "SerializeObject";

    /**
     * Create a String from the Object using Base64 encoding
     * @param object - any Object that is Serializable
     * @return - Base64 encoded string.
     */
    public static String objectToString(Serializable object) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(out).writeObject(object);
            byte[] data = out.toByteArray();
            out.close();

            out = new ByteArrayOutputStream();
            Base64OutputStream b64 = new Base64OutputStream(out,0);
            b64.write(data);
            b64.close();
            out.close();

            return new String(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a generic object that needs to be cast to its proper object
     * from a Base64 ecoded string.
     *
     * @param encodedObject
     * @return
     */
    public static Object stringToObject(String encodedObject) {
        try {
            return new ObjectInputStream(new Base64InputStream(
                    new ByteArrayInputStream(encodedObject.getBytes()), 0)).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}