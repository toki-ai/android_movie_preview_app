package com.example.mockproject.fragment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mockproject.R;
import com.example.mockproject.entities.Person;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private final List<Person> castList;

    public CastAdapter(List<Person> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cast_member, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Person cast = castList.get(position);
        holder.tvCastName.setText(cast.getName());
        if (cast.getImageUrl() != null) {
            Picasso.get().load(cast.getImageUrl()).into(holder.ivCastPhoto);
        } else {
            holder.ivCastPhoto.setImageResource(R.drawable.img_slash_bg);
        }
    }

    @Override
    public int getItemCount() {
        return castList.size();
    }

    static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCastPhoto;
        TextView tvCastName;

        CastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCastPhoto = itemView.findViewById(R.id.ivCastPhoto);
            tvCastName = itemView.findViewById(R.id.tvCastName);
        }
    }
}
