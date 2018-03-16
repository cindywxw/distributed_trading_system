public class TimerThread extends Thread{
    public void run(){
        while(true){
            
            try{
                Thread.sleep(60000);
            }
            catch (Exception e){
                System.out.println(e);
            }
            // System.out.println("TimerThread: 1 hour pass set other peer's time");
            Exchange.updateLogicTime();
        }

    }

}
