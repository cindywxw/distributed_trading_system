import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;


class BgPanel extends JPanel {
    Image bg = new ImageIcon("test1.jpg").getImage();
    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
    }
}

public class GraphicClient {
	
	static String hostName = "127.0.0.1";
	static int portNumber = 6000;
	private static Socket socket = null;
	private static BufferedReader in = null;
	private static PrintWriter out = null;
	private JComboBox<String> trades;
    private JTextField stockField;
    private JTextField priceField;
    private JTextField qtyField;
	
	
	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.err.println("Usage: java Client <portNumber>");
			System.exit(1);
		}

		portNumber = Integer.parseInt(args[0]);

		try {
			socket = new Socket(hostName, portNumber);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.println((char)27 +"[32mConnected successfully to Exchange on port " + portNumber + "!" + (char)27 + "[0m");
			socket.setSoTimeout(1000000);
			        
			new GraphicClient();	
				
		} catch (UnknownHostException e) {
			System.err.println((char)27 +"[31mDon't know about host " + hostName +(char)27 + "[0m");
			System.exit(1);

		} catch (Exception e) {
			System.out.println((char)27 +"[31mCouldn't get I/O for the connection to " +
					hostName);
			System.out.println("Please start the Exchange first." +(char)27 + "[0m");
			System.exit(1);
		}
    }	
	
	public GraphicClient() {
		JFrame guiFrame = new JFrame();
        //make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("Worldwide Stock Trading Sytem");
        guiFrame.setSize(480,400);
        guiFrame.setLocationRelativeTo(null);
        
        final JPanel bgPanel = new BgPanel();
        bgPanel.setLayout(new BorderLayout());      

        final JPanel tradePanel = new JPanel(new BorderLayout());
        tradePanel.setPreferredSize(new Dimension(480, 400));
        tradePanel.setMaximumSize(tradePanel.getPreferredSize());
        tradePanel.setMinimumSize(tradePanel.getPreferredSize());
        tradePanel.setOpaque(false);
        tradePanel.setLayout(new FlowLayout());
        
        // Options for the trading methods 
        String[] tradeMethods = {"Buy", "Sell", "Check Price", "Exit"};
        JLabel methodLbl = new JLabel("Method:", SwingConstants.CENTER);
        JLabel stockLbl = new JLabel("Stock Name:", SwingConstants.CENTER);
        JLabel priceLbl = new JLabel("Unit Price:(needed for buy or sell method)", SwingConstants.CENTER);
        JLabel qtyLbl = new JLabel("Quantity: (needed for buy or sell method)", SwingConstants.CENTER);
        trades = new JComboBox(tradeMethods);    
        stockField = new JTextField(30);
        priceField = new JTextField(10);
        qtyField = new JTextField(10);
        tradePanel.add(methodLbl);
        tradePanel.add(trades);
        tradePanel.add(stockLbl);
    	tradePanel.add(stockField);
    	tradePanel.add(priceLbl);
    	tradePanel.add(priceField);
    	tradePanel.add(qtyLbl);
    	tradePanel.add(qtyField);
        JButton operate = new JButton("Go");        
        operate.setBackground(Color.green); 
        operate.setOpaque(true);
        operate.setBorderPainted(false);
        tradePanel.add(operate);
        trades.setSelectedIndex(2);
        
        trades.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                String method = (String) combo.getSelectedItem();
                System.out.println(method);            
            }
        });

        operate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	String method = (String) trades.getSelectedItem();
            	
            	if(method.equalsIgnoreCase("exit")) {
            		out.close();
					System.exit(0);
            	} else {
            		String stock = stockField.getText();
            		String price = priceField.getText();
            		String quantity = qtyField.getText();
            		if (method.equalsIgnoreCase("buy") || method.equalsIgnoreCase("sell")) {
            			method = method.toLowerCase();
            			if(stock.isEmpty() || price.isEmpty() || quantity.isEmpty()) {
            				System.out.println((char)27 +"[31mWrong request format. Please try again."+ (char)27 + "[0m");
            				JOptionPane.showMessageDialog(tradePanel, 
            						"Wrong request format. Please enter full information and try again.", 
            						"Request declined.", JOptionPane.QUESTION_MESSAGE);
            			} else {
            				// send to exchange
            				GraphicClient.out.println(method + ":" + stock + ":" + price + ":" + quantity);
            				System.out.println(method + ":" + stock + ":" + price + ":" + quantity);
            				System.out.println("Request sent.");
    						String response;
    						try {
								response = in.readLine();
//								System.out.println("Response: " + response + ".");
	    						if(response.equals("success")) {
	    							System.out.println((char)27 +"[32mRequest accepted. "+ method + ":" + stock + ":" + price + ":" + quantity + (char)27 + "[0m");
	    							JOptionPane.showMessageDialog(tradePanel,
	    									"Response of "+ method + ":" + stock + ":" + price + ":" + quantity + "\n" +response+ ".",
	    									"Request accepted: "+ method + ":" + stock + ":" + price + ":" + quantity + ".", 
	    									JOptionPane.INFORMATION_MESSAGE);
	    						} else if(response.startsWith("Error:")){
	    							System.out.println((char)27 +"[31mRequest declined. Error message: " + response.split(":")[1] + (char)27 + "[0m");
	    							JOptionPane.showMessageDialog(tradePanel, 
	                						"Response of "+ method + ":" + stock + ":" + price + ":" + quantity + "\nError message: " + response.split(":")[1] + ".", 
	                						"Request declined.", JOptionPane.ERROR_MESSAGE);
	    						} else if(response.equals("fail")) {
	    							System.out.println((char)27 +"[31mRequest declined. " + response + " Please try again."+ (char)27 + "[0m");
	    							JOptionPane.showMessageDialog(tradePanel, 
	                						"Response of "+ method + ":" + stock + ":" + price + ":" + quantity + "\n" + response + ".", 
	                						"Request declined.", JOptionPane.ERROR_MESSAGE);
	    						}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								JOptionPane.showMessageDialog(tradePanel,
    									"Server is down. Please try again later.",
    									"Request declined.", JOptionPane.ERROR_MESSAGE);
								System.exit(1);
							}
    						
            			}
            		} else if (method.equalsIgnoreCase("check price")) {
            			method = "checkprice";
            			if(stock.isEmpty()) {
            				System.out.println((char)27 +"[31mWrong request format. Please enter the name of the stock try again."+ (char)27 + "[0m");
            				JOptionPane.showMessageDialog(tradePanel, 
            						"Wrong request format. Please enter the name of the stock and try again.", 
            						"Request declined.", JOptionPane.QUESTION_MESSAGE);
            			} else {
            				// send to exchange
            				GraphicClient.out.println(method + ":" + stock);
            				System.out.println("checkprice:" + stock);
            				System.out.println("Request sent.");
    						String response;
    						try {
								response = in.readLine();
//								System.out.println("Response: " + response + ".");
								if(response.startsWith("Error:")){
	    							System.out.println((char)27 +"[31mRequest declined. Error message: " + response.split(":")[1] + (char)27 + ".[0m");
	    							JOptionPane.showMessageDialog(tradePanel,
	    									"Error message: " + response.split(":")[1] + ".",
	    									"Request declined: "+method+stock, JOptionPane.ERROR_MESSAGE);
	    						} else {
	    							System.out.println((char)27 +"[32mRequest accepted. "+ method + ":" + stock + ".\nResponse: " + response + (char)27 + "[0m");
	    							JOptionPane.showMessageDialog(tradePanel,
	    									"Response of check price" + ":" + stock +"\nPrice:" + response,
	    									"Request accepted.", JOptionPane.INFORMATION_MESSAGE);
	    							trades.setSelectedIndex(0);;
	    							priceField.setText(response);
	    						} 
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								JOptionPane.showMessageDialog(tradePanel,
    									"Server is down. Please try again later.",
    									"Request declined.", JOptionPane.ERROR_MESSAGE);
								System.exit(1);
							}
    						
            			}
            		}
            	}
			}

        });
        
        bgPanel.add(tradePanel,BorderLayout.CENTER);        
        guiFrame.setContentPane(bgPanel);
        guiFrame.setVisible(true);
	}
}
