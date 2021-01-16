package com.jdjp.lazadashopeescanner.ui.order_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "OrdersAdapter";
    private static final int VIEW_TYPE_ORDER = 1;

    private List<Order> orders = new ArrayList<>();
    private Context context;

    public OrdersAdapter(Context context) {
        this.context = context;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ORDER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        if(viewType == VIEW_TYPE_ORDER) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_layout, parent, false);
            OrderViewHolder vh = new OrderViewHolder(v);
            return vh;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int itemType = holder.getItemViewType();

        if(itemType == VIEW_TYPE_ORDER) {
            OrderViewHolder vh = (OrderViewHolder) holder;
            vh.bind(orders.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        //views
        private View rootView;
        private TextView tvOrderNumber;
        private TextView tvStatus;
        private TextView tvStoreName;
        private TextView tvQuantity;


        OrderViewHolder(View v) {
            super(v);
            rootView = v;
            tvOrderNumber = v.findViewById(R.id.tvOrderNumber);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvStoreName = v.findViewById(R.id.tvStoreName);
            tvQuantity = v.findViewById(R.id.tvQuantity);

            initEvents();
        }

        public void bind(Order order) {

            int textColor = context.getResources().getColor(R.color.black);

            switch (order.getStatus()) {
                case "ready_to_ship":
                    textColor = context.getResources().getColor(R.color.green);
                    break;
                case "canceled":
                    textColor = context.getResources().getColor(R.color.red);
                    break;
                default:
                    textColor = context.getResources().getColor(R.color.black);
                    break;
            }

            tvOrderNumber.setText(order.getOrderNumber());
            tvStatus.setText(order.getDisplayStatus());
            tvStatus.setTextColor(textColor);
            tvStoreName.setText(order.getStoreName());
            tvQuantity.setText("Qty: " + order.getItemsCount());
        }

        public void initEvents() {
        }
    }
}
