package com.jdjp.lazadashopeescanner.ui.shop_accounts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.ShopAccount;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ShopAccountsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ShopAccountsAdapter";
    private static final int VIEW_TYPE_SHOP_ACCOUNT = 1;

    private List<ShopAccount> shopAccounts = new ArrayList<>();
    private Context context;
    private ButtonDeleteClickListener buttonDeleteClickListener;

    public ShopAccountsAdapter(Context context) {
        this.context = context;
    }

    public void setShopAccounts(List<ShopAccount> shopAccounts) {
        this.shopAccounts = shopAccounts;
        notifyDataSetChanged();
    }

    public void setButtonDeleteClickListener(ButtonDeleteClickListener buttonDeleteClickListener) {
        this.buttonDeleteClickListener = buttonDeleteClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_SHOP_ACCOUNT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        if(viewType == VIEW_TYPE_SHOP_ACCOUNT) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shop_account_layout, parent, false);
            ShopAccountViewHolder vh = new ShopAccountViewHolder(v);
            return vh;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int itemType = holder.getItemViewType();

        if(itemType == VIEW_TYPE_SHOP_ACCOUNT) {
            ShopAccountViewHolder vh = (ShopAccountViewHolder) holder;
            vh.bind(shopAccounts.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return shopAccounts.size();
    }

    public class ShopAccountViewHolder extends RecyclerView.ViewHolder {
        //views
        private View rootView;
        private TextView tvTitle;
        private TextView tvSubtitle;
        private ImageView imageViewIcon;
        private Button btnRemove;


        ShopAccountViewHolder(View v) {
            super(v);
            rootView = v;
            tvTitle = v.findViewById(R.id.tvTitle);
            tvSubtitle = v.findViewById(R.id.tvSubtitle);
            imageViewIcon = v.findViewById(R.id.imageViewIcon);
            btnRemove = v.findViewById(R.id.btnRemove);

            initEvents();
        }

        public void bind(ShopAccount shopAccount) {
            tvTitle.setText(shopAccount.getShortCode() + " - " + shopAccount.getAccount());

            String pattern = "MM-dd-yyyy hh:mm a";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(shopAccount.getExpiryDate());

            tvSubtitle.setText("Expires on: " + date);
        }

        public void initEvents() {
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonDeleteClickListener.onClick(shopAccounts.get(getLayoutPosition()));
                }
            });
        }
    }

    public interface ButtonDeleteClickListener {
        void onClick(ShopAccount shopAccount);
    }


}
