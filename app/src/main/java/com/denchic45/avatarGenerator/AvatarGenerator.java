package com.denchic45.avatarGenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class AvatarGenerator {

    public static final int DEF_AVATAR_SIZE = 192;
    private final Context context;
    private Rect rect;
    private Canvas canvas;
    private Paint paint;
    private final RandomColor randomColor = new RandomColor();

    public AvatarGenerator(Context context) {
        this.context = context;
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    private BitmapDrawable generateBitmapDrawable(String name, int size, int color, Typeface font) {
        size = size == 0 ? DEF_AVATAR_SIZE : size;
        rect = new Rect(0, 0, size, size);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (color == 0)
            paint.setColor(randomColor.getRandomColor());
        else
            paint.setColor(color);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawRect(rect, paint);

        name = formatText(name);
        TextPaint textPaint = createTextPainter(font, canvas);
        paint.getTextBounds(name, 0, name.length(), rect);
        int x = canvas.getWidth() / 2;
        int y = (int) (canvas.getHeight() / 2 - ((textPaint.descent() + textPaint.ascent()) / 2));

        drawTextCentred(name, size);
        canvas.drawText(name, x, y, textPaint);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    @NotNull
    private String formatText(@NotNull String name) {
        return name.substring(0, 1).toUpperCase();
    }

    @NotNull
    private TextPaint createTextPainter(Typeface font, @NotNull Canvas canvas) {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setAntiAlias(true);
        double relation = Math.sqrt(canvas.getWidth() * canvas.getHeight()) / 250;
        textPaint.setTextSize((float) (126 * relation));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        if (font != null)
            textPaint.setTypeface(font);
        return textPaint;
    }

    public void drawTextCentred(String text, float size) {
        paint.getTextBounds(text, 0, text.length(), rect);
        canvas.drawText(text, size - rect.exactCenterX(), size - rect.exactCenterY(), paint);
    }

    public static class Builder {
        private final Context context;
        private String name;
        private int color;
        private Typeface font;
        private int size;

        public Builder(Context context) {
            this.context = context;
            this.name = name;
        }

        public Builder initFrom(@NotNull AvatarBuilderInitializer initializer) {
            Builder builder = initializer.initBuilder(this);
            this.color = builder.getColor();
            this.font = builder.getFont();
            this.size = builder.getSize();
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder color(String color) {
            this.color = Color.parseColor(color);
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder font(Typeface font) {
            this.font = font;
            return this;
        }

        public BitmapDrawable generateBitmapDrawable() {
            return new AvatarGenerator(context).generateBitmapDrawable(name, size, color, font);
        }

        public byte[] generateBytes() {
            BitmapDrawable avatar = new AvatarGenerator(context).generateBitmapDrawable(name, size, color, font);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            avatar.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }

        public Context getContext() {
            return context;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public Typeface getFont() {
            return font;
        }

        public int getSize() {
            return size;
        }
    }

}
