package com.jdjp.lazadashopeescanner.ui.batch_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.jdjp.lazadashopeescanner.MainActivity;
import com.jdjp.lazadashopeescanner.R;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.ui.order_list.OrdersActivity;

import java.util.List;

public class BatchListActivity extends AppCompatActivity {
    private static final String TAG = "BatchListActivity";
    private static final int RC_VIEW_BATCH = 123;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private BatchesAdapter adapter;
    private ImageView imageViewNoRecord;

    private BatchesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_list);

        recyclerView = findViewById(R.id.recyclerView);
        imageViewNoRecord = findViewById(R.id.imageViewNoRecord);

        defineActionBar();
        initRecyclerView();

        //view model
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(BatchesViewModel.class);

        viewModel.getAllBatches().observe(this, new Observer<List<BatchWithExtraProps>>() {
            @Override
            public void onChanged(List<BatchWithExtraProps> batchWithExtraProps) {
                if(batchWithExtraProps.size() > 0) {
                    adapter.setBatches(batchWithExtraProps);
                    imageViewNoRecord.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    imageViewNoRecord.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }


            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new BatchesAdapter(this);

        adapter.setOnItemBatchClickedListener(new BatchesAdapter.OnItemBatchClicked() {
            @Override
            public void onItemBatchClicked(BatchWithExtraProps batchWithExtraProps) {
                Intent intent = new Intent(BatchListActivity.this, OrdersActivity.class);
                intent.putExtra("batchId", batchWithExtraProps.getBatch().getBatchId());
                startActivityForResult(intent, RC_VIEW_BATCH);
            }
        });

        adapter.setOnButtonDeleteClickedListener(new BatchesAdapter.OnButtonDeleteClicked() {
            @Override
            public void onButtonDeleteClicked(BatchWithExtraProps batchWithExtraProps) {
                new AlertDialog.Builder(BatchListActivity.this)
                        .setTitle("Delete Batch")
                        .setMessage("Do you want to delete batch " + batchWithExtraProps.getBatch().getBatchId() + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                viewModel.deleteBatch(batchWithExtraProps.getBatch());
                                viewModel.deleteAllOrdersByBatchId(batchWithExtraProps.getBatch().getBatchId());
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void defineActionBar() {

        setTitle("Records");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}