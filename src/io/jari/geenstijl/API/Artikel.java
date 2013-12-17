package io.jari.geenstijl.API;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * JARI.IO
 * Date: 15-12-13
 * Time: 17:59
 */
public class Artikel {
    public String titel;
    public String inhoud;
    public Drawable plaatje;
    public Boolean groot_plaatje;
    public Integer reacties;
    public Date datum;
    public String auteur;
    public Uri link;
}
