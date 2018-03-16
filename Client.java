
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

/**
 * The Client connects to the Exchange and send requests(buy, sell, check price) over TCP and receives
 * corresponding response. The "exit" command closes the socket.
 */
public class Client {
	static String hostName = "127.0.0.1";
	static int portNumber;

	public static void main(String[] args) throws IOException, InterruptedException {

		if (args.length != 1) {
			System.err.println(
					"Usage: java Client <portNumber>");
			System.exit(1);
		}

		portNumber = Integer.parseInt(args[0]);
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			socket = new Socket(hostName, portNumber);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.println((char)27 +"[32mConnected successfully to Exchange on port " + portNumber + "!" + (char)27 + "[0m");
			socket.setSoTimeout(1000000);

			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			while(stdIn != null) {
				System.out.print ("------------------------------------------------\n");
				System.out.print ("Usage of request methods: \n" +(char)27 +"[34mBuy\nSell\nCheckPrice\nExit\n"+ (char)27 + "[0m");
				System.out.print ("Please enter the request method you want: ");
				
				String request = stdIn.readLine();
			


				if(request.equalsIgnoreCase("buy")){
					System.out.print ("Please enter the stock name, unit price and quantity you want\n" +(char)27 +"[35m(<stockname>:<price>:<quantity>)"+ (char)27 + "[0m or "+ (char)27 +"[33mcancel" + (char)27 + "[0m:\n");
					request = stdIn.readLine();
					String[] splitReq = request.split(":");
					if(splitReq.length != 3) {
						if (!request.equalsIgnoreCase("cancel")) {
							System.out.println((char)27 +"[31mWrong request format. Please try again."+ (char)27 + "[0m");
						}
					} else {
						out.println("buy:" + request);
						System.out.println("Buy request sent.");
						String response;
						response = in.readLine();
//						System.out.println("Response: " + response + ".");
						if(response.equals("success")) {
							System.out.println((char)27 +"[32mRequest accepted."+ (char)27 + "[0m");
						} else if(response.startsWith("Error:")){
							System.out.println((char)27 +"[31mRequest declined. Error message: " + response.split(":")[1] + (char)27 + "[0m");
						} else{
							System.out.println((char)27 +"[31mRequest declined. Please try again."+ (char)27 + "[0m");
						}
					}

				} else if(request.equalsIgnoreCase("sell")){
					System.out.print ("Please enter the stock name, unit price and quantity you want\n" +(char)27 +"[35m(<stockname>:<price>:<quantity>)"+ (char)27 + "[0m or "+ (char)27 +"[33mcancel" + (char)27 + "[0m:\n");
					request = stdIn.readLine();
					String[] splitReq = request.split(":");
					if(splitReq.length != 3) {
						if (!request.equalsIgnoreCase("cancel")) {
							System.out.println((char)27 +"[31mWrong request format. Please try again."+ (char)27 + "[0m");
						}
					} else {
						out.println("sell:" + request);
						System.out.println("Sell request sent.");
						String response;
						response = in.readLine();
//						System.out.println("Response: " + response + ".");
						if(response.equals("success")) {
							System.out.println((char)27 +"[32mRequest accepted."+ (char)27 + "[0m");
						} else if(response.startsWith("Error:")){
							System.out.println((char)27 +"[31mRequest declined. Error message: " + response.split(":")[1] + (char)27 + "[0m");
						} else {
							System.out.println((char)27 +"[31mRequest declined. Please try again."+ (char)27 + "[0m");
						}
					}
				} else if (request.equalsIgnoreCase("checkprice")) {
					System.out.print ("Please enter the stock name you want check\n" +(char)27 +"[35m(<stockname>)"+ (char)27 + "[0m or "+ (char)27 +"[33mcancel" + (char)27 + "[0m:\n");
					request = stdIn.readLine();
					String[] splitReq = request.split(":");
					if(splitReq.length != 1) {
						System.out.println((char)27 +"[31mWrong request format. Please try again."+ (char)27 + "[0m");
					} else {
						if (!request.equalsIgnoreCase("cancel")) {
							out.println("checkprice:" + request);
							System.out.println("Check price request sent.");
							String response;
							response = in.readLine();
							System.out.println((char)27 +"[32m" + response +(char)27 + "[0m");
						}
					}
					
				} else if(request.equalsIgnoreCase("exit")) {
					out.close();
					in.close();
					socket.close();
					System.exit(0);
				} else {
					System.err.println("Invalid request.Please try again.");
					continue;
				}
			}
		} catch (UnknownHostException e) {
			System.err.println((char)27 +"[32mDon't know about host " + hostName +(char)27 + "[0m");
			System.exit(1);

		} catch (NullPointerException e){
			System.out.println("\n" + (char)27 +"[32mGoodbye!" + (char)27 + "[0m");
		} catch (Exception e) {
			System.err.println((char)27 +"[32mCouldn't get I/O for the connection to " +
					hostName);
			System.err.println("Please start the Client first." +(char)27 + "[0m");
			System.exit(1);
		}
	}      

}




