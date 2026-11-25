package com.example.login_register;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private final Context context;
    private final List<Order> orders = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));

    public OrdersAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<Order> orders) {
        this.orders.clear();
        this.orders.addAll(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        String id = order.getId() == null ? "" : order.getId();
        String suffix = id.substring(Math.max(0, id.length() - 6)).toUpperCase(Locale.ROOT);
        holder.orderId.setText("Đơn #" + suffix);
        Date createdAt = order.getCreatedAt();
        holder.orderDate.setText(createdAt == null ? "" : dateFormat.format(createdAt));
        String status = order.getStatus().isEmpty() ? "Đã thanh toán" : order.getStatus();
        holder.orderStatus.setText(status);
        holder.total.setText(PriceFormatter.format(order.getTotal()));
        holder.payment.setText("Thanh toán: " + order.getPaymentMethod());
        holder.items.removeAllViews();
        for (OrderItem item : order.getItems()) {
            holder.items.addView(createOrderLine(item));
        }
    }

    private View createOrderLine(OrderItem item) {
        View line = LayoutInflater.from(context).inflate(R.layout.item_order_line, null, false);
        ImageView image = line.findViewById(R.id.imgOrderProduct);
        TextView name = line.findViewById(R.id.tvOrderProductName);
        TextView variant = line.findViewById(R.id.tvOrderProductVariant);
        TextView price = line.findViewById(R.id.tvOrderProductPrice);

        int imageResId = context.getResources().getIdentifier(
                item.getImageName(), "drawable", context.getPackageName());
        if (imageResId == 0) {
            imageResId = R.drawable.ic_shoe_placeholder;
        }
        ProductImageUtil.apply(image, imageResId);
        name.setText(item.getName());
        variant.setText(item.getColorName() + " · Size EU " + item.getSize()
                + " · x" + item.getQuantity());
        price.setText("Đơn giá: " + PriceFormatter.format(item.getUnitPrice()));
        return line;
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static final class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView orderId;
        private final TextView orderDate;
        private final TextView orderStatus;
        private final LinearLayout items;
        private final TextView total;
        private final TextView payment;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.tvOrderId);
            orderDate = itemView.findViewById(R.id.tvOrderDate);
            orderStatus = itemView.findViewById(R.id.tvOrderStatus);
            items = itemView.findViewById(R.id.layoutOrderItems);
            total = itemView.findViewById(R.id.tvOrderTotal);
            payment = itemView.findViewById(R.id.tvOrderPayment);
        }
    }
}
