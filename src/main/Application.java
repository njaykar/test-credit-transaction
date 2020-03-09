package main;

import main.model.Transaction;
import main.util.TransactionTimeSorter;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Application {

    public static void main(String [] args){
        System.out.println("Enter path to file:");
        Scanner in = new Scanner(System.in);
        String fileName = in.next();

        Map<String, ArrayList<Transaction>> tranactionMap = new HashMap<>();

        try{
            BufferedReader csvReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = csvReader.readLine()) != null) {
                if(line.startsWith("\uFEFF")){
                    line = line.substring(1);
                }
                String [] values = line.split(",");
                String thisCardNumber = values[0].trim();

                Transaction thisTransaction = new Transaction();

                ZoneId zone = ZoneId.systemDefault();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(zone);
                thisTransaction.setTransactionTime(Instant.from(formatter.parse(values[1])));

                thisTransaction.setTransactionCost(Double.valueOf(values[2]));

                if(!tranactionMap.containsKey(thisCardNumber)){
                    ArrayList<Transaction> transactions = new ArrayList<>();
                    transactions.add(thisTransaction);
                    tranactionMap.put(thisCardNumber, transactions);
                }
                else{
                    tranactionMap.get(thisCardNumber).add(thisTransaction);
                }
            }
            System.out.println("OUTPUT");
            ArrayList<String> fraudCards = getFraudCards(tranactionMap, 100.0);
            System.out.println();
            fraudCards.forEach((fc) -> System.out.println("Card:" + fc));

//            tranactionMap.forEach((key, value) -> System.out.println(key + ":" + value.toString()));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> getFraudCards(Map<String, ArrayList<Transaction>> trasactionMap, Double threshold) {
        ArrayList<String> fraudCards = new ArrayList<>();

        for (Map.Entry<String, ArrayList<Transaction>> entry : trasactionMap.entrySet()) {
            if(!fraudCards.contains(entry.getKey())){
                ArrayList<Transaction> tList = entry.getValue();
                if(!tList.isEmpty()){
                    tList.sort(new TransactionTimeSorter());
                    Instant start = tList.get(0).getTransactionTime();
                    double dailyPurchase = 0;
                    for(int i = 0; i < tList.size(); i++) {
                        Transaction transaction = tList.get(i);
                        long isWithinADay = ChronoUnit.HOURS.between(start, transaction.getTransactionTime());
                        if(isWithinADay > 24) {
                            start = transaction.getTransactionTime();
                            dailyPurchase = 0;
                        }
                        else{
                            dailyPurchase += transaction.getTransactionCost();
                            if(dailyPurchase > threshold){
                                fraudCards.add(entry.getKey());
                            }
                        }
                    }
                }
            }
        }

        return fraudCards;
    }
}
