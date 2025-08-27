package com.example.itmba2_formative;

import androidx.recyclerview.widget.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.itmba2_formative.objects.Memory;
import java.util.List;
import java.util.Objects;

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

        // Load image from memory's photo
        if (memory.getPhotoUri() != null) {
            try {
                holder.imageView.setImageURI(memory.getPhotoUri());
                // Set scale type
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (SecurityException e) {
                // Placeholder
                holder.imageView.setImageResource(R.color.medium_text);
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        } else {
            // Placeholder
            holder.imageView.setImageResource(R.color.medium_text);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(memory);
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

    public void updateMemories(List<Memory> newMemories) {
        MemoryDiffCallback diffCallback = new MemoryDiffCallback(this.memories, newMemories);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.memories.clear();
        this.memories.addAll(newMemories);

        diffResult.dispatchUpdatesTo(this);
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_gallery_item);
        }
    }

    private static class MemoryDiffCallback extends DiffUtil.Callback {
        private final List<Memory> oldList;
        private final List<Memory> newList;

        MemoryDiffCallback(List<Memory> oldList, List<Memory> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Memory oldMemory = oldList.get(oldItemPosition);
            Memory newMemory = newList.get(newItemPosition);

            if (oldMemory.getPhotoUri() != null && newMemory.getPhotoUri() != null) {
                return oldMemory.getPhotoUri().equals(newMemory.getPhotoUri());
            }

            if (oldMemory.getPhotoUri() == null && newMemory.getPhotoUri() == null) {
                return oldMemory.equals(newMemory);
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Memory oldMemory = oldList.get(oldItemPosition);
            Memory newMemory = newList.get(newItemPosition);
            return Objects.equals(oldMemory, newMemory);
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }
}
