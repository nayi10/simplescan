package com.bandughana.simplescan;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import io.objectbox.Box;

public class ScanItemAdapter extends RecyclerView.Adapter<ScanItemAdapter.ScanItemViewHolder> {
    private List<ScanItem> scanItemList;
    private Context context;

    ScanItemAdapter(List<ScanItem> scanItemList, Context context) {
        this.scanItemList = scanItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ScanItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scan_item, parent, false);
        return new ScanItemViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanItemViewHolder holder, int position) {
        ScanItem item = scanItemList.get(position);
        if (item.getScannedText().equalsIgnoreCase("No scan results has been saved")){
            holder.btnDelete.setVisibility(View.GONE);
            holder.dateScannedView.setVisibility(View.GONE);
        }
        holder.bindData(item);

        holder.itemView.setOnClickListener(v -> {
            if(!holder.scanItemView.getText().toString()
                    .equalsIgnoreCase("No scan results has been saved")){

                LayoutInflater inflater = LayoutInflater.from(context);
                Dialog popupWindow = new Dialog(context);
                popupWindow.setTitle("Item Details");
                View mPopupView = Objects.requireNonNull(inflater).inflate(R.layout.item_dialog, null);
                Button btnUrl = mPopupView.findViewById(R.id.btnOpenUrl);
                Button btnShare = mPopupView.findViewById(R.id.btnShare);
                Button btnCopy = mPopupView.findViewById(R.id.btnCopy);
                TextView date = mPopupView.findViewById(R.id.date_scanned);
                TextView textView = mPopupView.findViewById(R.id.scan_text);

                popupWindow.addContentView(mPopupView, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                textView.setText(item.getScannedText());
                date.setText(item.getFormattedDate());

                if (URLUtil.isNetworkUrl(item.getScannedText())){
                    btnUrl.setText(context.getResources().getString(R.string.visit));
                } else {
                    btnUrl.setText(context.getResources().getString(R.string.search));
                }
                popupWindow.show();
                btnUrl.setOnClickListener(v13 -> {
                    popupWindow.dismiss();
                    dealWithUrl(item.getScannedText());
                });

                btnShare.setOnClickListener(v12 -> {
                    popupWindow.dismiss();
                    Helper.shareContent(item.getScannedText(), context);
                });
                btnCopy.setOnClickListener(v1 -> {
                    Helper.copyText(context, item.getScannedText());
                    Toast.makeText(context, "Copied to clipboard",
                            Toast.LENGTH_LONG).show();
                });
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            Box<ScanItem> itemBox = ObjectBox.get().boxFor(ScanItem.class);
            itemBox.remove(item);
            scanItemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, scanItemList.size());
        });
    }

    private void dealWithUrl(String url) {
        if (URLUtil.isNetworkUrl(url)) {
            Helper.openUrl(url, context);
        } else {
            Helper.openSearch(url, context);
        }
    }

    @Override
    public int getItemCount() {
        return scanItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return scanItemList.get(position).getId();
    }

    class ScanItemViewHolder extends RecyclerView.ViewHolder{
        TextView scanItemView;
        TextView dateScannedView;
        ImageButton btnDelete;

        ScanItemViewHolder(@NonNull View itemView) {
            super(itemView);
            scanItemView = itemView.findViewById(R.id.scan_value);
            dateScannedView = itemView.findViewById(R.id.date_scanned);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bindData(ScanItem item){
            scanItemView.setText(item.getScannedText());
            dateScannedView.setText(item.getFormattedDate());
        }
    }
}
