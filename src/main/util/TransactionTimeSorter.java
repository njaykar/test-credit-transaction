package main.util;

import main.model.Transaction;

import java.util.Comparator;

public class TransactionTimeSorter implements Comparator<Transaction> {

    @Override
    public int compare(Transaction t1, Transaction t2) {
        return t1.getTransactionTime().compareTo(t2.getTransactionTime());
    }
}
