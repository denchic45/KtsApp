package com.denchic45.kts.utils;

import android.content.Context;

import com.denchic45.kts.data.model.domain.ListItem;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public final class JsonUtil {
    public static List<ListItem> parseToList(@NotNull Context context, int resId) {
        final InputStream inputStream = context.getResources().openRawResource(resId);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
        return new GsonBuilder()
                .create()
                .fromJson(jsonString, new TypeToken<List<ListItem>>() {
                }.getType());
    }
}
