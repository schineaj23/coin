package com.asch.coin.ui;

import com.asch.coin.Core;
import com.asch.coin.Wallet;
import javafx.util.StringConverter;

public class WalletStringConverter extends StringConverter<Wallet> {
    @Override
    public String toString(Wallet wallet) {
        if(wallet != null)
            return wallet.getName();
        return "Select Wallet";
    }

    @Override
    public Wallet fromString(String s) {
        if(Core.wallets != null) {
            for(Wallet wallet : Core.wallets) {
                if(wallet.getName().equals(s))
                    return wallet;
            }
        }

        return null;
    }
}
