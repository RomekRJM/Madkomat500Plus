package com.example.madkomatapp.lego;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NXJCache {
    private static final String TAG = "NXJCache";

    public static void setup() {

        File root = new File(System.getenv("EXTERNAL_STORAGE"));

        try {
            String androidCacheFile = "nxj.cache";
            File mLeJOS_dir = new File(root + "/leJOS");
            if (!mLeJOS_dir.exists()) {
                mLeJOS_dir.mkdir();
            }
            File mCacheFile = new File(root + "/leJOS/", androidCacheFile);

            if (root.canWrite() && !mCacheFile.exists()) {
                FileWriter gpxwriter = new FileWriter(mCacheFile);
                BufferedWriter out = new BufferedWriter(gpxwriter);
                out.write("");
                out.flush();
                out.close();
                Log.i(TAG, "nxj.cache (record of connection addresses) written to: " + mCacheFile.getName());
            } else {
                Log.i(TAG, "nxj.cache file not written as "
                        + (!root.canWrite() ? mCacheFile.getPath()
                        + " can't be written to sdcard." : " cache already exists."));

            }
        } catch (IOException e) {
            Log.e(TAG, "Could not write nxj.cache " + e.getMessage(), e);
        }
    }
}
