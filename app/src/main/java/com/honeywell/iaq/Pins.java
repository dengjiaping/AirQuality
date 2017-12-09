package com.honeywell.iaq;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pins {

    public static void save(Context context, List<Item> items) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        JSONArray toSet = new JSONArray();
        for (Item item : items) {
            toSet.put(item.toJSON());
        }
        prefs.edit().putString("pins", toSet.toString()).apply();
    }

    public static void add(Context context, Item item) {
        List<Item> items = getAll(context);
        items.add(item);
        save(context, items);
    }

    public static List<Item> getAll(Context context) {
        List<Item> items = new ArrayList<Item>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String shortcuts = prefs.getString("pins", null);
        if (shortcuts == null) return items;
        try {
            JSONArray shortcutsJson = new JSONArray(shortcuts);
            for (int i = 0; i < shortcutsJson.length(); i++) {
                items.add(new Item(shortcutsJson.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static boolean contains(Context context, Item item) {
        List<Item> shortcuts = getAll(context);
        for (Item i : shortcuts) {
            if (i.toString().equals(item.toString())) return true;
        }
        return false;
    }

    public static void remove(Context context, int index) {
        List<Item> items = getAll(context);
        items.remove(index);
        save(context, items);
    }

    public static boolean remove(Activity context, String name) {
        List<Item> items = getAll(context);
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getName().equals(name)) {
                remove(context, i);
                return true;
            }
        }
        return false;
    }

    public static class Item {

        private String mName;
        private String mUrl;

        public Item(String name, String url) {
            mName = name;
            mUrl = url;
        }

        public Item(JSONObject json) {
            mName = json.optString("name");
            mUrl = json.optString("url");

        }

        public String getName() {
            return mName;
        }

        public String getUrl() {
            return mUrl;
        }

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            try {
                json.put("name", mName);
                json.put("url", mUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return json;
        }

        @Override
        public String toString() {
            return toJSON().toString();
        }
    }
}