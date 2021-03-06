package com.livermor.delegateadapter.delegate;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dumchev on 03.11.17.
 */
public class CompositeDelegateAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = CompositeDelegateAdapter.class.getSimpleName();
    private static final int FIRST_VIEW_TYPE = 0;

    protected final SparseArray<DelegateAdapter> typeToAdapterMap;
    protected final @NonNull
    List<T> data = new ArrayList<>();

    protected CompositeDelegateAdapter(@NonNull SparseArray<DelegateAdapter> typeToAdapterMap) {
        this.typeToAdapterMap = typeToAdapterMap;
    }

    @Override
    public final int getItemViewType(int position) {
        for (int i = FIRST_VIEW_TYPE; i < typeToAdapterMap.size(); i++) {
            final DelegateAdapter delegate = typeToAdapterMap.valueAt(i);
            //noinspection unchecked
            if (delegate.isForViewType(data, position)) {
                return typeToAdapterMap.keyAt(i);
            }
        }

        throw new NullPointerException("Provide adapter for type "
                + data.get(position).getClass() + " at position: " + position);
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return typeToAdapterMap.get(viewType).onCreateViewHolder(parent, viewType);
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final DelegateAdapter delegateAdapter = typeToAdapterMap.get(getItemViewType(position));
        //noinspection unchecked
        delegateAdapter.onBindViewHolder(holder, data, position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        //noinspection unchecked
        typeToAdapterMap.get(holder.getItemViewType()).onRecycled(holder);
    }

    public void swapData(@NonNull List<T> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public final int getItemCount() {
        return data.size();
    }

    public static class Builder<T> {

        private int count;
        private final SparseArray<DelegateAdapter> typeToAdapterMap;

        public Builder() {
            typeToAdapterMap = new SparseArray<>();
        }

        public Builder<T> add(@NonNull DelegateAdapter<?, ? extends T> delegateAdapter) {
            typeToAdapterMap.put(count++, delegateAdapter);
            return this;
        }

        public CompositeDelegateAdapter<T> build() {
            if (count == 0) {
                throw new IllegalArgumentException("Register at least one adapter");
            }
            return new CompositeDelegateAdapter<>(typeToAdapterMap);
        }
    }
}
