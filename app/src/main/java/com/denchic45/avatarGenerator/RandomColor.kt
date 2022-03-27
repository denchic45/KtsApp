package com.denchic45.avatarGenerator;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomColor {

    private final List<Integer> defColors = new ArrayList<Integer>() {{
        add(Color.parseColor("#303F9F"));
        add(Color.parseColor("#EF6C00"));
        add(Color.parseColor("#00bcd4"));
        add(Color.parseColor("#455A64"));
        add(Color.parseColor("#e53935"));
        add(Color.parseColor("#4caf50"));
        add(Color.parseColor("#004C3F"));
        add(Color.parseColor("#7E57C2"));
        add(Color.parseColor("#689f39"));
    }};

    public int getRandomColor() {
        return defColors.get(new Random().nextInt(defColors.size()));
    }
}
