package com.dsevSport.DSEV_Sport.common.util;

import java.math.BigDecimal;

public class SePayQrBuilder {

    public static String buildQR(String bank, String account, BigDecimal amount, String content) {
        return "https://qr.sepay.vn/img"
                + "?bank=" + bank
                + "&acc=" + account
                + "&amount=" + amount.toBigInteger()
                + "&des=" + content
                + "&template=compact";
    }
}
