package com.example.itmba2_formative;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.itmba2_formative.models.Memory;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private final List<Memory> memories;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Memory memory);
    }

    public GalleryAdapter(List<Memory> memories, OnItemClickListener listener) {
        this.memories = memories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        Memory memory = memories.get(position);

        // Load image from memory's photo URI
        if (memory.getPhotoUri() != null) {
            try {
                holder.imageView.setImageURI(memory.getPhotoUri());
                // Set scale type for consistent appearance
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (SecurityException e) {
                // Handle permission issues - show placeholder
                holder.imageView.setImageResource(R.color.medium_text);
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        } else {
            // No photo - show placeholder
            holder.imageView.setImageResource(R.color.medium_text);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(memory);

                // Award TES points for gallery interaction
                HelperMethods.updateTesScore(v.getContext(), AppConstants.TesScore.GALLERY_INTERACTION);
            }
        });

        // Set content description for accessibility
        holder.imageView.setContentDescription(memory.getTitle() != null && !memory.getTitle().isEmpty()
                ? memory.getTitle() : "Memory photo");
    }

    @Override
    public int getItemCount() {
        return memories.size();
    }

    /**
     * Update the adapter with new memories data
     */
    public void updateMemories(List<Memory> newMemories) {
        memories.clear();
        memories.addAll(newMemories);
        notifyDataSetChanged();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_gallery_item);
        }
    }
}