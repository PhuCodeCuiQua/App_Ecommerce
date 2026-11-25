package com.example.login_register;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.VH> {
    public interface Listener {
        void onDelete(Card card);
    }

    private final ArrayList<Card> items = new ArrayList<>();
    private final Listener listener;

    public CardAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(ArrayList<Card> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Card c = items.get(position);
        h.tvBrand.setText(c.brand);
        h.tvNumber.setText("•••• •••• •••• " + c.last4);
        h.tvHolder.setText(c.holder);
        h.tvExpiry.setText("Hết hạn " + c.expiry);
        h.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvBrand, tvNumber, tvHolder, tvExpiry, btnDelete;

        VH(@NonNull View v) {
            super(v);
            tvBrand = v.findViewById(R.id.tvCardBrand);
            tvNumber = v.findViewById(R.id.tvCardNumber);
            tvHolder = v.findViewById(R.id.tvCardHolder);
            tvExpiry = v.findViewById(R.id.tvCardExpiry);
            btnDelete = v.findViewById(R.id.btnCardDelete);
        }
    }
}