/* Copyright (C) 2015 Lookatitude IT Services & Consulting - All Rights Reserved.
* Unauthorized copying of this file, via any medium is strictly prohibited.
* 
* Proprietary and confidential.
*/

package org.gamaworks.banners.models;

import android.graphics.Bitmap;
import android.text.TextUtils;

/**
 * [mDescription]
 *
 * @author Filipe Ramos
 * @version 1.0
 */
public class Banner {
    @SuppressWarnings("unused")
    private static final String TAG = Banner.class.getSimpleName();

    public static final String BANNER_DIR_NAME = "Banners";

    private Bitmap mIcon;
    private String mIconWebAddress;
    private String mIconPath;
    private String mDescription;

    public Banner(Bitmap icon, String address, String description) {
        if (TextUtils.isEmpty(address)) throw new IllegalArgumentException();

        mIcon = icon;
        mIconWebAddress = address;
        mIconPath = BANNER_DIR_NAME + "/" + getFileName(address) + ".png";
        mDescription = description;
    }

    private String getFileName(String address) {
        int i = address.lastIndexOf("/");
        int j = address.lastIndexOf(".");
        if (j > 0) return address.substring(i + 1, j);
        return address.substring(i + 1);
    }

    @Override
    public String toString() {
        return "Image: " + mIconPath + " loaded from " + mIconWebAddress + " :: Description: " + mDescription;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public void setIcon(Bitmap icon) {
        this.mIcon = icon;
    }

    public String getIconPath() {
        return mIconPath;
    }

    public void setIconPath(String iconPath) {
        this.mIconPath = iconPath;
    }

    public String getIconWebAddress() {
        return mIconWebAddress;
    }

    public void setIconWebAddress(String iconWebAddress) {
        this.mIconWebAddress = iconWebAddress;
    }
}
