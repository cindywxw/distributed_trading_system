import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ClientThread extends Thread {
    protected Socket clientSocket;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                        
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("[Message Received] " + message);
                String[] parts = message.split(":");
                String operation = parts[0];

                // **************************
                // * Message handler switch * 
                // **************************

                String ret = "ack";
                String exchangeName, stockName;
                int exchangePort;
                Integer tmp;
                Double stockPrice;
                Integer qty;

                switch(operation){
                    case "getTime":
                        ret = (new Integer(Exchange.logicTime)).toString();
                        break;
                    case "setTime":
                        Exchange.setTime(Integer.parseInt(message.split(":")[1]));
                        break;

                    // New Exchange register to local superpeer
                    case "register":
                        exchangeName = parts[1];
                        exchangePort = -1;
                        try {
                            exchangePort = Integer.parseInt(parts[2]);
                        } catch (NumberFormatException e){
                            System.err.println("Invalid Register Port!");
                            System.exit(1);
                        }
                        Exchange.localExchangeAddr.put(exchangeName, exchangePort);

                        ret = (new Integer(Exchange.logicTime)).toString();
                        break;
                    case "registerReplicaToSuperPeer":
                        exchangeName = parts[1];
                        exchangePort = -1;
                        try {
                            exchangePort = Integer.parseInt(parts[2]);
                        } catch (NumberFormatException e){
                            System.err.println("Invalid Register Port!");
                            System.exit(1);
                        }
                        Exchange.localExchangeReplicaAddr.put(exchangeName, exchangePort);
                        ret = (new Integer(Exchange.logicTime)).toString();
                        break;
                    case "registerReplicaToHost":
                        Exchange.registerBackUp(Integer.parseInt(parts[1]));

                        ret = "";
                        // ret the current local stock infos
                        for (String stockNameIt : Exchange.localStockMap.keySet() ) {
                            Stock stock = Exchange.localStockMap.get(stockNameIt);
                            ret = ret + stockNameIt + ":" + stock.getPrice() + ":" + stock.getQty() + ":";
                        }
                        ret = ret + Exchange.logicTime;
                        break;
                    // Query the exchange address by name
                    case "query":
                        exchangeName = parts[1];
                        // Check whether is itself
                        if(exchangeName.equals(Exchange.name)) {
                            ret = Integer.toString(Exchange.port);
                            break;
                        }
                        // Check local address map
                        tmp = Exchange.localExchangeAddr.get(exchangeName);
                        ret = tmp == null ? "Error:Exchange not found" : tmp.toString();
                        break;

                    case "checkprice":
                        stockName = parts[1];
                        Double price;
                        // Check if it is mutual fund
                        if(Exchange.mutualFundMap.get(stockName) != null)
                            price = Exchange.getMutualFundPrice(stockName);
                        else
                            price = Exchange.getStockPrice(stockName);
                        ret = (price == null) ? "Error:Stock not found" : price.toString();
                        break;

                    case "buy":
                        stockName = parts[1];
                        try {
                            stockPrice = Double.parseDouble(parts[2]);
                            qty = Integer.parseInt(parts[3]);
                            // Check if it is mutual fund
                            if(Exchange.mutualFundMap.get(stockName) != null)
                                ret = Exchange.buyMutualFund(stockName, stockPrice, qty);
                            else
                                ret = Exchange.buy(stockName, stockPrice, qty);
                        } catch (NumberFormatException e){
                            ret = "Error:Invalid Value Format!";
                            break;
                        }
                        break;

                    case "sell":
                        stockName = parts[1];
                        try {
                            stockPrice = Double.parseDouble(parts[2]);
                            qty = Integer.parseInt(parts[3]);
                            ret = Exchange.sell(stockName, stockPrice, qty);
                        } catch (NumberFormatException e){
                            ret = "Error:Invalid Value Format!";
                            break;
                        }
                        break;

                    case "resume":
                        stockName = parts[1];
                        try {
                            qty = Integer.parseInt(parts[2]);
                            Exchange.resume(stockName, qty);
                        } catch (NumberFormatException e){
                            ret = "Error:Invalid Value Format!";
                            break;
                        }
                        break;

                    case "recover":
                        try{
                            Integer time = Integer.parseInt(parts[1]);
                            Exchange.setTime(time);
                        }
                        catch (NumberFormatException e){
                            ret = "Error:Invalid Value Format!";
                            break;
                        }
                        ret = Exchange.recover();
                        break;
                    case "putReplicaStockBook":
                        if (Exchange.isRP) {
                            stockName = parts[1];
                            try{
                                qty = Integer.parseInt(parts[2]);
                                stockPrice = Double.parseDouble(parts[3]);
                                Exchange.localStockMap.put(stockName,new Stock(stockPrice,qty));
                                
                            }
                            catch (NumberFormatException e){
                                ret = "Error:Invalid Value Format!";
                                break;
                            }
                        }
                        break;
                    case "localExchangeDownNotify":
                        try{
                            Integer port = Integer.parseInt(parts[1]);
                            Exchange.postServerDownMessage(port);
                        }
                        catch (NumberFormatException e){
                            ret = "Error:Invalid Value Format!";
                            break;
                        }
                        break;
  
                        
                    default:
                        System.err.println("Invalid Message Operation!");


                }
                System.out.println("[Message Replied] " + ret);
                out.println(ret);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}