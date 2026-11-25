package com.example.login_register;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final List<AppNotification> notifications = new ArrayList<>();
    private boolean allRead;

    public void submitList(List<AppNotification> items) {
        notifications.clear();
        notifications.addAll(items);
        allRead = false;
        notifyDataSetChanged();
    }

    public void markAllRead() {
        allRead = true;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification notification = notifications.get(position);
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        holder.icon.setImageResource(iconFor(notification.getType()));
        holder.unreadDot.setVisibility(notification.isRead() || allRead
                ? View.GONE : View.VISIBLE);
        if (notification.getCreatedAt() == null) {
            holder.time.setText("Vừa xong");
        } else {
            holder.time.setText(DateUtils.getRelativeTimeSpanString(
                    notification.getCreatedAt().getTime(), System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS));
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private int iconFor(String type) {
        if ("order".equals(type)) {
            return R.drawable.ic_cart;
        }
        if ("promo".equals(type)) {
            return R.drawable.ic_discount;
        }
        return R.drawable.ic_notifications;
    }

    static final class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView message;
        private final TextView time;
        private final ImageView icon;
        private final View unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvNotificationTitle);
            message = itemView.findViewById(R.id.tvNotificationMessage);
            time = itemView.findViewById(R.id.tvNotificationTime);
            icon = itemView.findViewById(R.id.imgNotificationIcon);
            unreadDot = itemView.findViewById(R.id.viewNotificationUnread);
        }
    }
}
