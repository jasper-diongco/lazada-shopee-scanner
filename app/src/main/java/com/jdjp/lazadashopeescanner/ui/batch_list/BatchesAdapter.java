package com.jdjp.lazadashopeescanner.ui.batch_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BatchesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final String TAG = "OrdersAdapter";
        private static final int VIEW_TYPE_BATCH = 1;

        private List<BatchWithExtraProps> batches = new ArrayList<>();
        private Context context;
        private OnItemBatchClicked onItemBatchClickedListener;
        private OnButtonDeleteClicked onButtonDeleteClickedListener;

        public BatchesAdapter(Context context) {
            this.context = context;
        }

    public void setOnItemBatchClickedListener(OnItemBatchClicked onItemBatchClickedListener) {
        this.onItemBatchClickedListener = onItemBatchClickedListener;
    }

    public void setOnButtonDeleteClickedListener(OnButtonDeleteClicked onButtonDeleteClickedListener) {
        this.onButtonDeleteClickedListener = onButtonDeleteClickedListener;
    }

    public void setBatches(List<BatchWithExtraProps> batches) {
            this.batches = batches;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return VIEW_TYPE_BATCH;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // create a new view
            if (viewType == VIEW_TYPE_BATCH) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_batch_layout, parent, false);
                BatchViewHolder vh = new BatchViewHolder(v);
                return vh;
            }

            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int itemType = holder.getItemViewType();

            if (itemType == VIEW_TYPE_BATCH) {
                BatchViewHolder vh = (BatchViewHolder) holder;
                vh.bind(batches.get(position));
            }

        }

        @Override
        public int getItemCount() {
            return batches.size();
        }

        public class BatchViewHolder extends RecyclerView.ViewHolder {
            //views
            private View rootView;
            private TextView tvBatchNumber;
            private TextView tvParcelsCount;
            private Button btnDelete;


            BatchViewHolder(View v) {
                super(v);
                rootView = v;
                tvBatchNumber = v.findViewById(R.id.tvBatchNumber);
                tvParcelsCount = v.findViewById(R.id.tvParcelsCount);
                btnDelete = v.findViewById(R.id.btnDelete);

                initEvents();
            }

            public void bind(BatchWithExtraProps batch) {

                String pattern = "MM-dd-yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(batch.getBatch().getCreatedAt());

                tvBatchNumber.setText("Batch ID: " + batch.getBatch().getBatchId() + " | " + date);
                tvParcelsCount.setText("Total Parcels: " + batch.getScanCount());

            }

            public void initEvents() {
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemBatchClickedListener.onItemBatchClicked(batches.get(getLayoutPosition()));
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onButtonDeleteClickedListener.onButtonDeleteClicked(batches.get(getLayoutPosition()));
                    }
                });
            }
        }

        public interface OnItemBatchClicked {
            void onItemBatchClicked(BatchWithExtraProps batchWithExtraProps);
        }

    public interface OnButtonDeleteClicked {
        void onButtonDeleteClicked(BatchWithExtraProps batchWithExtraProps);
    }
}
