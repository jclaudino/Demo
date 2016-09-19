package com.baboolian.demo.util;

import com.baboolian.demo.model.MediaStoreFile;

import org.json.JSONArray;

import java.util.List;

public class MediaStoreUtil {

    //SQLite doesn't support array types, so we store the list of file IDs as a JSONArray String
    public static String getFileIds(List<MediaStoreFile> files) {
        JSONArray jsonArray = new JSONArray();
        for (MediaStoreFile file : files) {
            jsonArray.put(file.getId());
        }
        return jsonArray.toString();
    }

}
