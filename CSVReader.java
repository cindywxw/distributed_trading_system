import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CSVReader{
    // ****************************************
    // * read the CSV and get local data list *
    // ****************************************
        
    public static void setExchangeData(){
        String filename = "price_stocks.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try{
            br = new BufferedReader(new FileReader(filename));

            br.readLine();
            br.readLine();
            br.readLine();
            while((line = br.readLine()) != null ){
                ArrayList<String> lines = csvParseLine(line);

                // set the stock to exchange map
                Exchange.stockToExchange.put(lines.get(3),lines.get(2));

                if (lines.get(2).equals(Exchange.name)){
                    ArrayList<Double> newList = new ArrayList<Double>();

                    for (int j = 4; j < lines.size() ; j ++)
                    {
                        newList.add(Double.parseDouble(lines.get(j)));
                    }
                    Exchange.stockPriceBook.put(lines.get(3),newList);
                    Exchange.localStockMap.put(lines.get(3),new Stock());
                    // System.out.println("Add stock " + lines.get(3) + " of price value: " + newList.size());
                }
            }
            // System.out.println(Exchange.stockToExchange);
        }
        catch (Exception e){
          System.out.println(e);

        }


        filename = "qty_stocks.csv";
        try{
            br = new BufferedReader(new FileReader(filename));
            br.readLine();
            br.readLine();

            line = br.readLine();

            // read the exchange name, get indexes
            ArrayList<String> lines = csvParseLine(line);
            HashMap<Integer,String> stockIndexes = new HashMap<Integer,String>();
            for (int i=0; i < lines.size(); i++) {
                if(lines.get(i).equals(Exchange.name))
                {
                    stockIndexes.put(i,null);
                }
            }

            line = br.readLine();

            // read the stock name
            lines = csvParseLine(line);
            for (int i : stockIndexes.keySet()) {
                stockIndexes.put(i, lines.get(i));
            }

            int lineTime = 0;
            while((line = br.readLine()) != null){
                lines = csvParseLine(line);
                for ( int i : stockIndexes.keySet()){
                    if (!lines.get(i).equals("")) {
                        ArrayList<Integer> newList = new ArrayList<Integer>();
                        newList.add(lineTime);
                        newList.add(Integer.parseInt(lines.get(i)));
                        newList.add(0);
                        Exchange.stockQtyBook.put(stockIndexes.get(i),newList);
                    }
                }
                lineTime ++;
            }
        }

        catch (Exception e){
          System.out.println(e);

        }

        // log
        // for (String i : stockQtyBook.keySet()) {
        //     System.out.println("Stock " + i + " will add qty " + stockQtyBook.get(i).get(1).toString() + " at time :" + stockQtyBook.get(i).get(0).toString());
        // }

    }

    // ******************
    // * parse csv line *
    // ******************

    public static ArrayList<String> csvParseLine(String cvsLine) {

        ArrayList<String> result = new ArrayList<>();
        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;
        char[] chars = cvsLine.toCharArray();
        for (char ch : chars) {
            if (inQuotes) {
                startCollectChar = true;
                if (ch == '"') {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }
        }
        result.add(curVal.toString());
        return result;
    }


}