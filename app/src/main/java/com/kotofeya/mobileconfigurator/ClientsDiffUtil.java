package com.kotofeya.mobileconfigurator;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

public class ClientsDiffUtil extends DiffUtil.Callback {
    private final List<String> oldResults;
    private final List<String> newResults;

    public ClientsDiffUtil(List<String> oldResults, List<String> newResults) {
        this.oldResults = oldResults;
        this.newResults = newResults;
    }

    @Override
    public int getOldListSize() {
        return oldResults.size();
    }

    @Override
    public int getNewListSize() {
        return newResults.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        String oldCustomScanResult = oldResults.get(oldItemPosition);
        String  newCustonScanResult = newResults.get(newItemPosition);
        return oldCustomScanResult.equals(newCustonScanResult);
    }

//    сравниваем только то, что видно в gui
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        String oldCustomScanResult = oldResults.get(oldItemPosition);
        String  newCustonScanResult = newResults.get(newItemPosition);
        return oldCustomScanResult.equals(newCustonScanResult);
    }
}
