import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


public class Exchange {

    public static String name;
    public static int port;
    private static int superPeerPort;
    private static int replicaPort;
    private static Boolean isSP;
    public static Boolean isRP;

    // csv data
    public static HashMap<String,ArrayList<Double>> stockPriceBook = new HashMap<String,ArrayList<Double>>();
    public static HashMap<String,ArrayList<Integer>> stockQtyBook = new HashMap<String,ArrayList<Integer>>();

    // stock status data
    public static ConcurrentHashMap<String, Stock> localStockMap = new ConcurrentHashMap<String, Stock>();
    public static HashMap<String,String> stockToExchange = new HashMap<String,String>();

    // mutual funds data
    final public static MutualFundMap mutualFundMap = new MutualFundMap();

    // Superpeer data
    private static ArrayList<Integer> superPeerList = new ArrayList<Integer>();
    static {
        superPeerList.add(6000); // Europe
        superPeerList.add(6100); // America
        superPeerList.add(6200); // Asia
        superPeerList.add(6300); // Africa
    }
    private static ArrayList<Integer> superPeerReplicaList = new ArrayList<Integer>();
    static {
        superPeerReplicaList.add(7000); // Europe
        superPeerReplicaList.add(7100); // America
        superPeerReplicaList.add(7200); // Asia
        superPeerReplicaList.add(7300); // Africa
    }

    private static Integer timerSuperPeerPort = 6000;
    public static HashMap<String,Integer> localExchangeAddr = new HashMap<String,Integer>();
    public static HashMap<String,Integer> localExchangeReplicaAddr = new HashMap<String,Integer>();

    // time
    public static int logicTime; 

    // back up server info
    public static int mirrorPort = -1;

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java Exchange <Name> <Port> <SuperPeerPort>");
            System.exit(1);
        }

        // Parse args
        name = args[0];
        isSP = false;
        try {
            port = Integer.parseInt(args[1]);
            superPeerPort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e){
            System.err.println("Usage: java Exchange <Name> <Port> <SuperPeerPort> [host server port]");
            System.exit(1);
        }


        // set the stock price book
        CSVReader.setExchangeData();

        // set replica info
        if (args.length == 4)
        {
            isRP = true;
            try{
                registerBackUp(Integer.parseInt(args[3]));
            }
            catch (NumberFormatException e){
                System.err.println("Usage: java Exchange <Name> <Port> <SuperPeerPort> [host server port]");
                System.exit(1);
            }
            
            String ret = send("registerReplicaToHost:"+port,mirrorPort);
            String[] parts = ret.split(":");
            for (int i = 0; i + 2 < parts.length ; i += 3) {
                try{
                    Double stockPrice = Double.parseDouble(parts[i+1]);
                    Integer qty = Integer.parseInt(parts[i+2]);
                    localStockMap.put(parts[i],new Stock(stockPrice,qty));
                }
                catch(NumberFormatException e){
                }
            }

            // printLocalStock();
        }
        else
            isRP = false;


        if(port == superPeerPort)
        {
            isSP = true;
            if (port == timerSuperPeerPort)
            {
                setTime(0);
                (new TimerThread()).start();
                System.out.println("Timer server: logic time start with value: 0 ");
            }
            else
            {
                try{
                    setTime(Integer.parseInt(send("getTime:", timerSuperPeerPort)));
                }
                catch(NumberFormatException e){
                    System.out.println("Error: timer server not found, exit");
                    System.exit(1);
                }
            }
        }
        else
        {
            // Register to local superPeer
            if (!isRP)
                setTime(Integer.parseInt(send("register:" + name + ":" + port, superPeerPort)));
            else
                setTime(Integer.parseInt(send("registerReplicaToSuperPeer:" + name + ":" + port,superPeerPort)));
        }

        // Starts the Server
        try (
            ServerSocket serverSocket = new ServerSocket(port);
        ){
            System.out.println((char)27 + "[32mServer Starts Successfully!" + (char)27 + "[0m");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    new ClientThread(clientSocket).start();
                } catch (IOException e) {
                    System.out.println("I/O error: " + e);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    // *****************************************
    // * set the super peer's peers logic time *
    // *****************************************

    public static void setTime(int time)
    {
        logicTime = time;
        if (isSP){
            for (int localExchangePort : localExchangeAddr.values()) {
                send("setTime:"+time,localExchangePort);
            }
        }

        // modify the stock qty
        for(String stock : stockQtyBook.keySet())
        {
            if (stockQtyBook.get(stock).get(2) == 1) {
                continue;
            }
            if (stockQtyBook.get(stock).get(0) <= logicTime) {
                localStockMap.get(stock).setQty(localStockMap.get(stock).getQty() + stockQtyBook.get(stock).get(1));
                putReplicaStockBook(stock,localStockMap.get(stock));
                stockQtyBook.get(stock).set(2,1);
            }
        }

        // modify the stock price
        for (String stock: stockPriceBook.keySet()) {
            localStockMap.get(stock).setPrice(stockPriceBook.get(stock).get(logicTime));
        }
        // printLocalStock();
        

    }

    // *******************************************
    // * update logic time (by time server 6000) *
    // *******************************************

    public static void updateLogicTime(){
        logicTime ++;

        for (int i = 1;i < 4 ; i++ ) {
            send("setTime:" + logicTime,superPeerList.get(i));
        }

        setTime(logicTime);
    }

    // ******************************
    // * send messages to endpoints *
    // ******************************

    public static String send(String message, int serverAddr) {
        String ret = null;
        try {
            // try to open a socket
            Socket clientSocket = new Socket("127.0.0.1", serverAddr);
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            ) {
                System.out.println("[Message Sent] " + message);
                out.println(message);
                ret = in.readLine();
                clientSocket.close();

            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection.\n");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(ret == null)
        {
            if (postServerDownMessage(serverAddr)){
                ret = "Error:Remote server is DOWN, please wait for it to recover.";
            }
            else
                ret = "Error:Remote server is DOWN.";
        }
        return ret;
    }

    // **************************
    // * print local stock info *
    // **************************
    public static void printLocalStock(){
        System.out.println("System time: " + logicTime);
        for (String stock : localStockMap.keySet() ) {
            System.out.print(stock + " :");
            localStockMap.get(stock).print();
        }
    }


    
    // *****************************
    // * get port by exchange name *
    // *****************************

    public static int getExchangeAddr(String name) {
        // First check local address map
        if(localExchangeAddr.get(name) == null) {
            // Request Other SuperPeers
            int ret = -1;
            for (int spAddr : superPeerList) {
                if(spAddr == port) continue; // not self 
                String resp = send("query:" + name, spAddr);
                if(resp.startsWith("Error:") || resp.equals("notfound")) continue;
                try {
                    ret = Integer.parseInt(resp);
                    break;
                } catch (NumberFormatException e){
                    System.err.println("Invalid Query Result!");
                    System.exit(1);
                }
            }
            return ret;
        } else {
            return localExchangeAddr.get(name);
        }
    }

    // ***********************************
    // * get exchagne port by stock name *
    // ***********************************

    public static int getExchangeAddrByStock(String stockName) {
        // Get the exchange name of the stock
        String exchangeName = stockToExchange.get(stockName);
        if(exchangeName == null) return -1;

        // Get the address of the exchange
        return getExchangeAddr(exchangeName);
    }

    // ****************************************
    // * get stock/mutual fund price globally *
    // ****************************************

    public static Double getStockPrice(String stockName) {
        // Check local stocks
        Stock stock = localStockMap.get(stockName);
        if(stock != null) return stock.getPrice();

        // Get the address of the exchange that has this stock 
        int exchangePort = getExchangeAddrByStock(stockName);
        if (exchangePort == -1) return null;
        
        // Send message to the remote address to check price
        String resp = Exchange.send("checkprice:"+stockName, exchangePort);
        Double price;
        try {
            price = Double.parseDouble(resp);
        } catch (NumberFormatException e){
            // System.err.println("Invalid Price Received!");
            return null;
        }
        return price;
    }

    public static Double getMutualFundPrice(String mutualFundName) {
        Double avePrice = 0.0;
        MutualFundElement elem;
        Iterator<MutualFundElement> iter = mutualFundMap.get(mutualFundName).iterator();
        while (iter.hasNext()){
            elem = iter.next();
            Double price = getStockPrice(elem.getStockName());
            if (price == null) return null;
            avePrice += price * elem.getPercentage();
        }
        return twoDigits(avePrice);
    }

    // *************
    // * buy stock *
    // *************

    public static String buy(String stockName, Double price, int qty) {
        String success = "";

        while(true) {
            // Check whether it is local stock
            Stock originalStock = localStockMap.get(stockName);
            if(originalStock != null) {
                // Use replace() method to keep it safe!!!
                // Check whether remaining qty is enough and price is correct
                if (!originalStock.getPrice().equals(price)) {
                    return "Error:Wrong price provided.";
                } else if (originalStock.getQty() < qty) {
                    return "Error:Not enough stock remains.";
                } else {
                    Stock newStock = new Stock(originalStock.getPrice(), originalStock.getQty() - qty);
                    success = localStockMap.replace(stockName, originalStock, newStock) ? "success" : "Error:Unable to update stock.";
                    if (success.equals("success")){
                        putReplicaStockBook(stockName,newStock);
                        System.out.println("[Buy Request] Stock: " + stockName + ", Remain: " + localStockMap.get(stockName).getQty());
                        break;
                    }
                }
            } else {
                // Get the address of the exchange that has this stock 
                int exchangePort = getExchangeAddrByStock(stockName);
                if (exchangePort == -1) return "Error:Unknown stock";
                
                success = send("buy:"+stockName+":"+price + ":" + qty, exchangePort);
                break;
            }
        }
        return success;
    }

    public static void resume(String stockName, int qty) {
        Stock originalStock = localStockMap.get(stockName);
        if(originalStock != null) {
            // Use replace() method to keep it safe!!!
            System.out.println("[Resume Request] Stock: " + stockName + ", Resume: " + qty + ", Remain: " + originalStock.getQty());
            Stock newStock = new Stock(originalStock.getPrice(), originalStock.getQty() + qty);

            // Make sure it succeed            
            while(!localStockMap.replace(stockName, originalStock, newStock)) {
                System.out.println("[Resume Retry] Stock: " + stockName + ", Resume: " + qty + ", Remain: " + originalStock.getQty());
                try{
                    Thread.sleep(1000);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            putReplicaStockBook(stockName,newStock);
        }
    }

    public static String buyMutualFund(String mutualFundName, Double price, int qty) {
        // Check qty
        if(qty % 100 != 0)
            return "Error:Qty should be a mutiplier of 100.";

        // +============================+
        // | Two Phase Commit Algorithm |
        // +============================+

        boolean success = true;
        Double avePrice = 0.0;
        String ret = "success";

        // +=============+
        // | First Phase |
        // +=============+
        
        MutualFundElement elem;
        Iterator<MutualFundElement> iter = mutualFundMap.get(mutualFundName).iterator();
        ArrayList<MutualFundElement> purchasedStocks = new ArrayList<MutualFundElement>();

        while (iter.hasNext()){
            elem = iter.next();
            String stockName = elem.getStockName();
            Double curPrice = getStockPrice(elem.getStockName());
            if (curPrice != null) {
                success = false;
                ret = "Error:Cannot buy " + stockName + " since the server is down, please wait for recover!"; 
                break;
            }
            String resp = buy(stockName, curPrice , (int)(qty * elem.getPercentage()));
            if(!resp.equals("success")) {
                success = false;
                ret = "Error:Cannot buy " + qty * elem.getPercentage() + " shares of "+ stockName;
                break;
            } else {
                purchasedStocks.add(elem);
            }
            avePrice += curPrice * elem.getPercentage();
        }

        avePrice = twoDigits(avePrice);
        // Check Price
        if(success && !avePrice.equals(price)) {
            success = false;
            ret = "Error:Prices Changed!";
        }

        // +==============+
        // | Second Phase |
        // +==============+
        
        if(!success) {
            // Return all the purchased stocks
            for(MutualFundElement stock : purchasedStocks) {
                send("resume:" + stock.getStockName() + ":" + (int) (stock.getPercentage() * qty), getExchangeAddrByStock(stock.getStockName()));
            }
        }

        return ret;
    }

    // **************
    // * sell stock *
    // **************

    public static String sell(String stockName, Double price, int qty) {
        String success = "";

        while(true) {
            // Check mutual funds
            // Check whether it is local stock
            Stock originalStock = localStockMap.get(stockName);
            if(originalStock != null) {
                // Use replace() method to keep it safe!!!
                if (!originalStock.getPrice().equals(price)) {
                    return "Error:Wrong price provided.";
                } else {
                    Stock newStock = new Stock(originalStock.getPrice(), originalStock.getQty() + qty);
                    success = localStockMap.replace(stockName, originalStock, newStock) ? "success" : "Error:Unable to update stock.";
                    if (success.equals("success")) {
                        putReplicaStockBook(stockName, newStock);
                        System.out.println("[Sell Request] Stock: " + stockName + ", Remain: " + localStockMap.get(stockName).getQty());
                        break;
                    }
                }
                
            } else {
                // Get the address of the exchange that has this stock 
                int exchangePort = getExchangeAddrByStock(stockName);
                if (exchangePort == -1) return "Error:Unknown stock or server is DOWN";
                success = send("sell:"+stockName+":"+price + ":" + qty, exchangePort);
                break;
            }
        }
        return success;
    }

    // **************************
    // * alert a server is down *
    // **************************

    public static boolean postServerDownMessage(Integer port){
        System.out.println("[Replica] Server " + port + " is down");
        if (isSP) {
            for (String exchangeName: localExchangeAddr.keySet()) {
                if (localExchangeAddr.get(exchangeName).equals(port)){
                    if (localExchangeReplicaAddr.keySet().contains(exchangeName)) {
                        System.out.println("[Replica] Trying to recover exchange " + exchangeName + "from replica in port" + localExchangeReplicaAddr.get(exchangeName));
                        recoverLocalExchange(exchangeName);
                        return true;
                    }        
                }
            }
        
            System.out.println("[Replica] No back up server found, abort...");
            return false;
        }
        else{
            send("localExchangeDownNotify:" + port, superPeerPort);

        }
        return false;
    }

    // ****************************
    // * recover a local exchange *
    // ****************************
    public static void recoverLocalExchange(String exchangeName){
        // send message to make the replica server as former server
        Integer replicaPort = localExchangeReplicaAddr.get(exchangeName);
        send("recover:" + logicTime,replicaPort);

        // set the address book
        localExchangeAddr.put(exchangeName,replicaPort);
        localExchangeReplicaAddr.remove(exchangeName);
        System.out.println("[Replica] exchange " + exchangeName + " has change to new port " + replicaPort);
    }


    // ****************************
    // * save two digit of double *
    // ****************************

    public static Double twoDigits(Double in){
        return Math.floor(in * 100) / 100;

    }

    // **************************
    // * register backup server *
    // **************************

    public static void registerBackUp(Integer replicaPort){
        mirrorPort = replicaPort;
        if (!isRP){
            System.out.println("[Replica] Host: Register mirror server at port "+mirrorPort);
        }
        else{
            System.out.println("[Replica] Backup: Register to host at port "+mirrorPort);
        }
    }

    // ***************************
    // * put in replicaStockBook *
    // ***************************

    public static void putReplicaStockBook(String name,Stock stock){
        if (mirrorPort == -1) {
            // System.out.println("[Replica] Host: mirror server not found");
        }
        else if (!isRP) {
            String ret = send("putReplicaStockBook:"+name+":"+stock.getQty() + ":" + stock.getPrice(),mirrorPort);
        }
    }

    // ***********
    // * recover *
    // ***********

    public static String recover(){
        if (isRP) {
            mirrorPort = -1;
            isRP = false;
            return "success";
        }
        else{
            return "Error:Not Replica server";
        }
    }

}
