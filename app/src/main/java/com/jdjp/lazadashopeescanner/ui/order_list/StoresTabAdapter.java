package com.jdjp.lazadashopeescanner.ui.order_list;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.OrderItem;
import com.jdjp.lazadashopeescanner.model.pojo.StoreWithOrders;
import com.jdjp.lazadashopeescanner.services.OrderService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class StoresTabAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ProblemSolutionsAdapter";

    private Context context;
    private List<StoreWithOrders> storeWithOrdersList = new ArrayList<>();
    public OnSwipeRefreshListener onSwipeRefreshListener;

    public static final int VIEW_TYPE_STORE_TAB = 1;

    public StoresTabAdapter(Context context) {
        this.context = context;
    }

    public void setStoreWithOrdersList(List<StoreWithOrders> storeWithOrdersList) {
        this.storeWithOrdersList = storeWithOrdersList;
        notifyDataSetChanged();
    }

    public void setOnSwipeRefreshListener(OnSwipeRefreshListener onSwipeRefreshListener) {
        this.onSwipeRefreshListener = onSwipeRefreshListener;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_STORE_TAB;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_STORE_TAB) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.store_tab_layout, parent, false);
            StoreTabViewHolder vh = new StoreTabViewHolder(v);
            return vh;
        }

        return null;

    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int itemType = holder.getItemViewType();

        if(VIEW_TYPE_STORE_TAB == itemType) {
            StoreTabViewHolder vh = (StoreTabViewHolder) holder;
            StoreWithOrders storeWithOrders = storeWithOrdersList.get(position);
            vh.bind(storeWithOrders);
        }
    }

    @Override
    public int getItemCount() {
        return storeWithOrdersList.size();
    }

    public class StoreTabViewHolder extends RecyclerView.ViewHolder {
        private View root;
        private RecyclerView recyclerView;
        private OrdersAdapter ordersAdapter;
        private TextView tvReadyToShipCount;
        private TextView tvCancelledCount;
        private TextView tvTotalScan;
        private TextView tvShippedCount;
        private TextView tvDeliveredCount;
        private TextView tvReturnedCount;
        private TextView tvLostBy3plCount;
        private TextView tvFailedCount;
        private TextView tvShippingBackCount;
        private TextView tvLastRefresh;
        private SwipeRefreshLayout swipeRefreshLayout;


        StoreTabViewHolder(View v) {
            super(v);

            root = v;
            recyclerView = v.findViewById(R.id.recyclerView);
            tvReadyToShipCount = v.findViewById(R.id.tvReadyToShipCount);
            tvCancelledCount = v.findViewById(R.id.tvCancelledCount);
            tvTotalScan = v.findViewById(R.id.tvTotalScan);
            tvDeliveredCount = v.findViewById(R.id.tvDeliveredCount);
            tvShippedCount = v.findViewById(R.id.tvShippedCount);
            tvReturnedCount = v.findViewById(R.id.tvReturnedCount);
            tvLostBy3plCount = v.findViewById(R.id.tvLostBy3plCount);
            tvFailedCount = v.findViewById(R.id.tvFailedCount);
            tvShippingBackCount = v.findViewById(R.id.tvShippingBackCount);
            tvLastRefresh = v.findViewById(R.id.tvLastRefresh);
            swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);

            initRecyclerView();
            initEvents();
        }

        public void bind(StoreWithOrders storeWithOrders) {
            ordersAdapter.setOrders(storeWithOrders.getSortedOrders());
            displaySummaryByStore(storeWithOrders.getSortedOrders());
            displayLastRefresh(storeWithOrders.getSortedOrders());
        }

        private void initEvents() {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onSwipeRefreshListener.onRefresh(getLayoutPosition(), swipeRefreshLayout);
                }
            });
        }

        private void initRecyclerView() {
            recyclerView.setHasFixedSize(true);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);

            ordersAdapter = new OrdersAdapter(context);

            recyclerView.setAdapter(ordersAdapter);
        }

        private void displaySummaryByStore(List<Order> orders) {
            int readyToShipCount = 0;
            int canceledCount = 0;
            int shippedCount = 0;
            int deliveredCount = 0;
            int returnedCount = 0;
            int lostBy3plCount = 0;
            int failedCount = 0;
            int INFO_ST_DOMESTIC_RETURN_WITH_LAST_MILE_3PL = 0;

            for (int i = 0 ; i < orders.size(); i++) {
                if(orders.get(i).getStatus().contains("ready_to_ship")) {
                    readyToShipCount++;
                } else if(orders.get(i).getStatus().contains("canceled")) {
                    canceledCount++;
                } else if(orders.get(i).getStatus().contains("failed")) {
                    failedCount++;
                } else if(orders.get(i).getStatus().contains("LOST_BY_3PL")) {
                    lostBy3plCount++;
                } else if(orders.get(i).getStatus().contains("INFO_ST_DOMESTIC_RETURN_WITH_LAST_MILE_3PL")) {
                    INFO_ST_DOMESTIC_RETURN_WITH_LAST_MILE_3PL++;
                } else if(orders.get(i).getStatus().contains("returned")) {
                    returnedCount++;
                } else if(orders.get(i).getStatus().contains("shipped")) {
                    shippedCount++;
                } else if(orders.get(i).getStatus().contains("delivered")) {
                    deliveredCount++;
                }
            }

            tvReadyToShipCount.setText(String.valueOf(readyToShipCount));
            tvCancelledCount.setText(String.valueOf(canceledCount));
            tvTotalScan.setText(String.valueOf(orders.size()));
            tvShippedCount.setText(String.valueOf(shippedCount));
            tvDeliveredCount.setText(String.valueOf(deliveredCount));
            tvReturnedCount.setText(String.valueOf(returnedCount));
            tvLostBy3plCount.setText(String.valueOf(lostBy3plCount));
            tvFailedCount.setText(String.valueOf(failedCount));
            tvShippingBackCount.setText(String.valueOf(INFO_ST_DOMESTIC_RETURN_WITH_LAST_MILE_3PL));
        }

        private void displayLastRefresh(List<Order> orders) {
            if(orders.size() <= 0) return;

            String pattern = "MM-dd-yyyy hh:mm:ss a";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(orders.get(0).getFetchedAt());
            tvLastRefresh.setText("Last Refresh: " + date);
        }
    }

    public interface OnSwipeRefreshListener {
        void onRefresh(int index, SwipeRefreshLayout swipeRefreshLayout);
    }
}
