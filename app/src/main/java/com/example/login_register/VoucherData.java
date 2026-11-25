package com.example.login_register;

import java.util.ArrayList;
import java.util.List;

public final class VoucherData {
    private VoucherData() {
    }

    public static List<Voucher> eligibleVouchers(double subtotal) {
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(Voucher.NONE);
        vouchers.add(new Voucher("GIAM10", "GIAM10 - Giảm 10%, tối đa 300.000 đ",
                0.10, 300000, 0, false));
        if (subtotal >= 1000000) {
            vouchers.add(new Voucher("GIAM100K", "GIAM100K - Giảm 100.000 đ",
                    0, 0, 100000, false));
        }
        vouchers.add(new Voucher("FREESHIP", "FREESHIP - Miễn phí vận chuyển",
                0, 0, 0, true));
        return vouchers;
    }

    public static boolean isEligible(Voucher selected, double subtotal) {
        for (Voucher voucher : eligibleVouchers(subtotal)) {
            if (voucher.getId().equals(selected.getId())) {
                return true;
            }
        }
        return false;
    }
}
