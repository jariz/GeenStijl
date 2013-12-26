package io.jari.geenstijl.API;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.Serializable;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * JARI.IO
 * Date: 15-12-13
 * Time: 17:59
 */
public class Artikel implements Serializable {
    public String titel;
    public String inhoud;
    public byte[] plaatje; /* byte for serialization reasons */
    public Boolean groot_plaatje;
    public Integer reacties;
    public Date datum;
    public String auteur;
    public String link;
    public String embed;
    public Comment[] comments;
    public Integer id;
}
