package com.example.login_register;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.VH> {
    public interface Listener {
        void onEdit(Address a);
        void onDelete(Address a);
        void onSetDefault(Address a);
    }

    private final ArrayList<Address> items = new ArrayList<>();
    private final Listener listener;

    public AddressAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(ArrayList<Address> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Address a = items.get(position);
        h.tvName.setText(a.name);
        h.tvPhone.setText(a.phone);
        h.tvDetail.setText(a.detail);
        h.tvDefault.setVisibility(a.isDefault ? View.VISIBLE : View.GONE);
        h.btnDefault.setVisibility(a.isDefault ? View.GONE : View.VISIBLE);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(a));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(a));
        h.btnDefault.setOnClickListener(v -> listener.onSetDefault(a));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvDetail, tvDefault, btnEdit, btnDelete, btnDefault;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvAddrName);
            tvPhone = v.findViewById(R.id.tvAddrPhone);
            tvDetail = v.findViewById(R.id.tvAddrDetail);
            tvDefault = v.findViewById(R.id.tvAddrDefault);
            btnEdit = v.findViewById(R.id.btnAddrEdit);
            btnDelete = v.findViewById(R.id.btnAddrDelete);
            btnDefault = v.findViewById(R.id.btnAddrSetDefault);
        }
    }
}