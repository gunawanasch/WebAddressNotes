package com.webaddressnotes.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.webaddressnotes.R;
import com.webaddressnotes.database.Database;
import com.webaddressnotes.model.WebAddress;

import java.util.ArrayList;

/**
 * Created by gunawan on 03/11/18.
 */

public class WebAddressAdapter extends RecyclerView.Adapter<WebAddressAdapter.ViewHolder> {
    private ArrayList<WebAddress> wa = new ArrayList<>();
    private Activity activity;
    private Database db;
    private Dialog dialogCustomAddMinum;
    private OnItemClickListener mListener;

    public WebAddressAdapter(Activity activity, ArrayList<WebAddress> wa) {
        this.wa = wa;
        this.activity = activity;
    }

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.row_web_address, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WebAddressAdapter.ViewHolder viewHolder, int position) {
        viewHolder.tvName.setText(wa.get(position).getName());
        viewHolder.tvAddress.setText(wa.get(position).getAddress());
        viewHolder.cardView.setOnClickListener(onClickListener(position));
    }

    private View.OnClickListener onClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url  = wa.get(position).getAddress();
                openToBrowser(v, url);
            }
        };
    }

    public void openToBrowser(View v, String url){
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        v.getContext().startActivity(intent);
        try {
            v.getContext().startActivity(intent);
        }
        catch (Exception e){
            if (url.startsWith("http://")) {
                openToBrowser(v, url.replace("http://","https://"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != wa ? wa.size() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvAddress;
        private ImageView ivOptionMenu;
        private View cardView;

        public ViewHolder(View view, final OnItemClickListener listener) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvAddress = (TextView) view.findViewById(R.id.tvAddress);
            ivOptionMenu = (ImageView) view.findViewById(R.id.ivOptionMenu);
            cardView = view.findViewById(R.id.cardView);

            ivOptionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        final int position = getAdapterPosition();
                        Log.e("data adapter", ""+wa.get(position).getName());
                        if(position != RecyclerView.NO_POSITION) {
                            final Context context = ivOptionMenu.getContext();
                            PopupMenu popup = new PopupMenu(context, ivOptionMenu);
                            popup.inflate(R.menu.option_menu_list);
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_edit:
                                            Log.e("edit", ""+wa.get(position).getName());
                                            listener.onEditClick(position);
                                            return true;
                                        case R.id.action_delete:
                                            Log.e("delete", ""+wa.get(position).getName());
                                            listener.onDeleteClick(position);
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                            popup.show();
                        }
                    }
                }
            });
        }
    }
}
