package com.multipleimagescamera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viktoria on 1/17/17.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ImageHolder> {
    private List<String> photoList = new ArrayList<>();
    private Context context;

    public GalleryAdapter(Context context) {
        this.context = context;
    }

    public void addPhotos(List<String> photoList) {
        this.photoList.addAll(photoList);
        notifyDataSetChanged();
    }

    public List<String> getPhotoList() {
        return photoList;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, int position) {
        Glide.with(context).load(new File(photoList.get(position))).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }


    class ImageHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image)
        ImageView image;

        ImageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
