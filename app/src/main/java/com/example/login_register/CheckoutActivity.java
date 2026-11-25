package com.example.login_register;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.text.InputType;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class CheckoutActivity extends AppCompatActivity {
    private static final double STANDARD_SHIPPING = 30000;
    private static final double EXPRESS_SHIPPING = 50000;
    private static final String[] PAYMENT_METHODS = {
            "Thanh toán khi nhận hàng (COD)",
            "Thẻ ghi nợ/tín dụng",
            "Ví điện tử",
            "Chuyển khoản ngân hàng"
    };

    private final List<MaterialCardView> shippingCards = new ArrayList<>();
    private final List<TextView> shippingLabels = new ArrayList<>();
    private final List<ImageView> shippingChecks = new ArrayList<>();
    private final List<MaterialCardView> paymentCards = new ArrayList<>();
    private final List<TextView> paymentLabels = new ArrayList<>();
    private final List<ImageView> paymentChecks = new ArrayList<>();
    private View topbar;
    private View bottomBar;
    private LinearLayout itemsContainer;
    private LinearLayout priceProductsContainer;
    private LinearLayout shippingContainer;
    private LinearLayout paymentContainer;
    private TextView tvVoucher;
    private TextView tvAddress;
    private TextView tvSubtotal;
    private TextView tvShippingCost;
    private TextView tvVoucherDiscount;
    private TextView tvGrandTotal;
    private MaterialCardView cardVoucher;
    private MaterialButton btnPay;
    private int topbarBasePadding;
    private int bottomBarBasePadding;
    private int selectedShipping;
    private int selectedPayment;
    private Voucher selectedVoucher = Voucher.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_checkout);
        initViews();
        setupInsets();
        setupTopbar();
        setupItemsSection();
        setupVoucher();
        setupAddress();
        setupShipping();
        setupPayment();
        setupPricingDetails();
        setupPayButton();
        loadCart();
    }

    private void initViews() {
        topbar = findViewById(R.id.topbarSimple);
        bottomBar = findViewById(R.id.checkoutBottomBar);
        topbarBasePadding = topbar.getPaddingTop();
        bottomBarBasePadding = bottomBar.getPaddingBottom();
        itemsContainer = findViewById(R.id.layoutCheckoutItems);
        priceProductsContainer = findViewById(R.id.layoutPriceProducts);
        shippingContainer = findViewById(R.id.layoutShippingOptions);
        paymentContainer = findViewById(R.id.layoutPaymentOptions);
        cardVoucher = findViewById(R.id.cardVoucher);
        tvVoucher = findViewById(R.id.tvVoucher);
        tvAddress = findViewById(R.id.tvAddress);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingCost = findViewById(R.id.tvShippingCost);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        btnPay = findViewById(R.id.btnPay);
    }

    private void setupInsets() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), findViewById(R.id.main));
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, 0);
            topbar.setPadding(topbar.getPaddingLeft(), topbarBasePadding + systemBars.top,
                    topbar.getPaddingRight(), topbar.getPaddingBottom());
            bottomBar.setPadding(bottomBar.getPaddingLeft(), bottomBar.getPaddingTop(),
                    bottomBar.getPaddingRight(), bottomBarBasePadding + systemBars.bottom);
            return insets;
        });
    }

    private void setupTopbar() {
        ((TextView) findViewById(R.id.tvTitle)).setText("Thanh toán");
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void setupItemsSection() {
        renderCheckoutItems();
    }

    private void renderCheckoutItems() {
        itemsContainer.removeAllViews();
        for (CartItem item : CartManager.getItems()) {
            itemsContainer.addView(createCheckoutItem(item));
        }
    }

    private View createCheckoutItem(CartItem item) {
        MaterialCardView card = new MaterialCardView(this);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.setRadius(getResources().getDimension(R.dimen.radius_medium));
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.md_surface));
        card.setCardElevation(getResources().getDimension(R.dimen.card_elevation));
        card.setStrokeWidth(0);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dimen(R.dimen.space_12), dimen(R.dimen.space_12),
                dimen(R.dimen.space_12), dimen(R.dimen.space_12));

        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                dimen(R.dimen.cart_image_size), dimen(R.dimen.cart_image_size));
        imageParams.setMarginEnd(dimen(R.dimen.space_12));
        image.setLayoutParams(imageParams);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setBackgroundColor(ContextCompat.getColor(this, R.color.product_tile));
        ProductImageUtil.apply(image, item.getColorImageResId());
        row.addView(image);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        row.addView(details, detailsParams);

        TextView name = createText(item.getProduct().getName(),
                R.style.TextAppearance_App_Checkout_TitleSmall, R.color.md_onSurface);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);
        details.addView(name);
        String variant = item.getColorName() + " · Size EU " + item.getSize()
                + " · x" + item.getQuantity();
        details.addView(createText(variant, R.style.TextAppearance_App_Checkout_BodySmall,
                R.color.md_onSurfaceVariant));

        TextView amount = createText(PriceFormatter.format(
                        item.getProduct().getSalePrice() * item.getQuantity()),
                R.style.TextAppearance_App_Checkout_BodyMedium, R.color.md_onSurface);
        amount.setTypeface(amount.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        amountParams.setMarginStart(dimen(R.dimen.space_8));
        row.addView(amount, amountParams);
        card.addView(row);
        return card;
    }

    private void setupVoucher() {
        updateVoucherLabel();
        cardVoucher.setOnClickListener(view -> showVoucherDialog());
    }

    private void showVoucherDialog() {
        List<Voucher> eligible = VoucherData.eligibleVouchers(CartManager.getTotal());
        CharSequence[] labels = new CharSequence[eligible.size()];
        int selectedIndex = 0;
        for (int i = 0; i < eligible.size(); i++) {
            labels[i] = eligible.get(i).getName();
            if (eligible.get(i).getId().equals(selectedVoucher.getId())) {
                selectedIndex = i;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chọn voucher")
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> {
                    selectedVoucher = eligible.get(which);
                    updateVoucherLabel();
                    recalcTotals();
                    dialog.dismiss();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateVoucherLabel() {
        tvVoucher.setText(Voucher.NONE.getId().equals(selectedVoucher.getId())
                ? "Chọn voucher" : selectedVoucher.getName());
    }

    private void setupAddress() {
        renderAddress();
        findViewById(R.id.btnChangeAddress).setOnClickListener(view -> showAddressDialog());
    }

    private void renderAddress() {
        String address = CheckoutState.address == null ? "" : CheckoutState.address.trim();
        tvAddress.setText(address.isEmpty() ? "Chưa có địa chỉ giao hàng" : address);
    }

    private void showAddressDialog() {
        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        inputLayout.setHint("Địa chỉ giao hàng");
        inputLayout.setBoxCornerRadii(dimen(R.dimen.field_radius), dimen(R.dimen.field_radius),
                dimen(R.dimen.field_radius), dimen(R.dimen.field_radius));
        TextInputEditText input = new TextInputEditText(inputLayout.getContext());
        input.setMinLines(3);
        input.setMaxLines(5);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setText(CheckoutState.address);
        inputLayout.addView(input);
        inputLayout.setPadding(dimen(R.dimen.space_8), dimen(R.dimen.space_8),
                dimen(R.dimen.space_8), 0);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Địa chỉ giao hàng")
                .setView(inputLayout)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    CheckoutState.address = input.getText() == null
                            ? "" : input.getText().toString().trim();
                    renderAddress();
                    recalcTotals();
                })
                .show();
    }

    private void setupShipping() {
        addChoiceOption(shippingContainer, shippingCards, shippingLabels, shippingChecks,
                "Giao tiêu chuẩn (3-5 ngày) · " + PriceFormatter.format(STANDARD_SHIPPING),
                () -> {
                    selectedShipping = 0;
                    updateChoiceStyles(shippingCards, shippingLabels, shippingChecks,
                            selectedShipping);
                    recalcTotals();
                });
        addChoiceOption(shippingContainer, shippingCards, shippingLabels, shippingChecks,
                "Giao nhanh (1-2 ngày) · " + PriceFormatter.format(EXPRESS_SHIPPING),
                () -> {
                    selectedShipping = 1;
                    updateChoiceStyles(shippingCards, shippingLabels, shippingChecks,
                            selectedShipping);
                    recalcTotals();
                });
        updateChoiceStyles(shippingCards, shippingLabels, shippingChecks, selectedShipping);
    }

    private void setupPayment() {
        for (int i = 0; i < PAYMENT_METHODS.length; i++) {
            final int index = i;
            addChoiceOption(paymentContainer, paymentCards, paymentLabels, paymentChecks,
                    PAYMENT_METHODS[i], () -> {
                        selectedPayment = index;
                        updateChoiceStyles(paymentCards, paymentLabels, paymentChecks,
                                selectedPayment);
                    });
        }
        updateChoiceStyles(paymentCards, paymentLabels, paymentChecks, selectedPayment);
    }

    private void addChoiceOption(LinearLayout container, List<MaterialCardView> cards,
                                 List<TextView> labels, List<ImageView> checks,
                                 String text, Runnable onSelected) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dimen(R.dimen.space_8);
        card.setLayoutParams(cardParams);
        card.setRadius(getResources().getDimension(R.dimen.radius_medium));
        card.setCardElevation(0);
        card.setStrokeWidth(dimen(R.dimen.card_stroke_width));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dimen(R.dimen.button_height));
        row.setPadding(dimen(R.dimen.card_padding), dimen(R.dimen.space_8),
                dimen(R.dimen.card_padding), dimen(R.dimen.space_8));

        TextView label = createText(text, R.style.TextAppearance_App_Checkout_BodyMedium,
                R.color.md_onSurface);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        row.addView(label, labelParams);

        ImageView check = new ImageView(this);
        check.setLayoutParams(new LinearLayout.LayoutParams(
                dimen(R.dimen.icon_size), dimen(R.dimen.icon_size)));
        check.setImageResource(R.drawable.ic_check);
        row.addView(check);
        card.addView(row);
        card.setOnClickListener(view -> onSelected.run());
        container.addView(card);
        cards.add(card);
        labels.add(label);
        checks.add(check);
    }

    private void updateChoiceStyles(List<MaterialCardView> cards, List<TextView> labels,
                                    List<ImageView> checks, int selectedIndex) {
        int dark = ContextCompat.getColor(this, R.color.panel_dark);
        int light = ContextCompat.getColor(this, R.color.md_surface);
        int onDark = ContextCompat.getColor(this, R.color.white);
        int onLight = ContextCompat.getColor(this, R.color.md_onSurface);
        int outline = ContextCompat.getColor(this, R.color.md_outline);
        for (int i = 0; i < cards.size(); i++) {
            boolean selected = i == selectedIndex;
            cards.get(i).setCardBackgroundColor(selected ? dark : light);
            cards.get(i).setStrokeColor(selected ? dark : outline);
            cards.get(i).setStrokeWidth(selected ? 0 : dimen(R.dimen.card_stroke_width));
            labels.get(i).setTextColor(selected ? onDark : onLight);
            checks.get(i).setVisibility(selected ? View.VISIBLE : View.GONE);
            checks.get(i).setImageTintList(ColorStateList.valueOf(onDark));
        }
    }

    private void setupPricingDetails() {
        renderPriceProducts();
        recalcTotals();
    }

    private void renderPriceProducts() {
        priceProductsContainer.removeAllViews();
        for (CartItem item : CartManager.getItems()) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = createText(item.getProduct().getName() + " x"
                            + item.getQuantity(), R.style.TextAppearance_App_Checkout_BodySmall,
                    R.color.md_onSurfaceVariant);
            label.setMaxLines(1);
            row.addView(label, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView dots = createText("................................",
                    R.style.TextAppearance_App_Checkout_BodySmall, R.color.md_outline);
            dots.setMaxLines(1);
            LinearLayout.LayoutParams dotsParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            dotsParams.setMarginStart(dimen(R.dimen.space_4));
            dotsParams.setMarginEnd(dimen(R.dimen.space_4));
            row.addView(dots, dotsParams);

            TextView amount = createText(PriceFormatter.format(
                            item.getProduct().getSalePrice() * item.getQuantity()),
                    R.style.TextAppearance_App_Checkout_BodySmall, R.color.md_onSurface);
            row.addView(amount);
            priceProductsContainer.addView(row);
        }
    }

    private void recalcTotals() {
        double subtotal = CartManager.getTotal();
        if (!VoucherData.isEligible(selectedVoucher, subtotal)) {
            selectedVoucher = Voucher.NONE;
            updateVoucherLabel();
        }
        double shipping = selectedVoucher.isFreeShipping() ? 0
                : selectedShipping == 0 ? STANDARD_SHIPPING : EXPRESS_SHIPPING;
        double discount = selectedVoucher.calculateDiscount(subtotal);
        double total = Math.max(0, subtotal + shipping - discount);
        tvSubtotal.setText(PriceFormatter.format(subtotal));
        tvShippingCost.setText(PriceFormatter.format(shipping));
        tvVoucherDiscount.setText(discount > 0
                ? "-" + PriceFormatter.format(discount) : PriceFormatter.format(0));
        tvGrandTotal.setText(PriceFormatter.format(total));
    }

    private void setupPayButton() {
        btnPay.setOnClickListener(view -> {
            String address = CheckoutState.address == null
                    ? "" : CheckoutState.address.trim();
            if (address.isEmpty()) {
                Snackbar.make(findViewById(R.id.main),
                        "Vui lòng nhập địa chỉ giao hàng", Snackbar.LENGTH_LONG).show();
                return;
            }
            btnPay.setEnabled(false);
            OrderRepository.saveOrder(this, createOrder(address), () -> {
                btnPay.setEnabled(true);
                showOrderSuccess();
            }, () -> {
                btnPay.setEnabled(true);
                Snackbar.make(findViewById(R.id.main),
                        "Không thể lưu đơn hàng, vui lòng thử lại",
                        Snackbar.LENGTH_LONG).show();
            });
        });
    }

    private Order createOrder(String address) {
        double subtotal = CartManager.getTotal();
        double shippingFee = selectedVoucher.isFreeShipping() ? 0
                : selectedShipping == 0 ? STANDARD_SHIPPING : EXPRESS_SHIPPING;
        double discount = selectedVoucher.calculateDiscount(subtotal);
        double total = Math.max(0, subtotal + shippingFee - discount);
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : CartManager.getItems()) {
            String imageName = getResources()
                    .getResourceEntryName(cartItem.getColorImageResId());
            Product product = cartItem.getProduct();
            orderItems.add(new OrderItem(product.getId(), product.getName(),
                    cartItem.getColorName(), cartItem.getSize(), cartItem.getQuantity(),
                    product.getSalePrice(), imageName));
        }
        String shippingMethod = selectedShipping == 0
                ? "Giao tiêu chuẩn (3-5 ngày)" : "Giao nhanh (1-2 ngày)";
        String paymentMethod = selectedPayment >= 0
                && selectedPayment < PAYMENT_METHODS.length
                ? PAYMENT_METHODS[selectedPayment] : PAYMENT_METHODS[0];
        return new Order("", new Date(), "Đã thanh toán", subtotal, shippingFee, discount,
                selectedVoucher.getId(), shippingMethod, paymentMethod, address, total,
                orderItems);
    }

    private void showOrderSuccess() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đặt hàng thành công")
                .setMessage("Tổng thanh toán: " + tvGrandTotal.getText())
                .setCancelable(false)
                .setPositiveButton("Về trang chủ", (dialog, which) -> {
                    CartManager.clear();
                    Intent intent = new Intent(this, ListItemActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private TextView createText(String text, int textAppearance, int textColor) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextAppearance(textAppearance);
        view.setTextColor(ContextCompat.getColor(this, textColor));
        return view;
    }

    private int dimen(int resource) {
        return getResources().getDimensionPixelSize(resource);
    }

    private void loadCart() {
        CartManager.load(this, () -> {
            renderCheckoutItems();
            renderPriceProducts();
            recalcTotals();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (itemsContainer != null) {
            renderCheckoutItems();
            renderPriceProducts();
            recalcTotals();
            renderAddress();
        }
    }
}
