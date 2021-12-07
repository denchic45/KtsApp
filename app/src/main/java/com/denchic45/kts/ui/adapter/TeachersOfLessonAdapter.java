package com.denchic45.kts.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.denchic45.kts.R;
import com.denchic45.kts.data.model.domain.User;

import java.util.List;


public class TeachersOfLessonAdapter extends ArrayAdapter<User> {

    public TeachersOfLessonAdapter(@NonNull Context context, @NonNull List<User> objects) {
        super(context, R.layout.item_user, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        }
        User item = getItem(position);
        ImageView ivAvatar = convertView.findViewById(R.id.iv_avatar);
        TextView tvUserFullName = convertView.findViewById(R.id.tv_teacher_full_name);
        Glide.with(parent.getContext())
                .load(item.getPhotoUrl())
                .into(ivAvatar);
        tvUserFullName.setText(item.getFullName());
        return convertView;
    }
}
