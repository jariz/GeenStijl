/*
 * Copyright 2014 Jari Zwarts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public byte[] background; /*background header powned*/
    public Integer reacties;
    public Date datum;
    public String auteur;
    public String link;
    public String embed;
    public Comment[] comments;
    public Integer id;
    public Boolean summary;
}
