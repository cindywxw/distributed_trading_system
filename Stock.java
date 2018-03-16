public class Stock{
    private Integer qty;
    private Double price;

    public Stock(){
        qty = 0;
    }

    public Stock(Double price, Integer qty){
        this.price = price;
        this.qty = qty;
    }

    public void setQty(Integer q){
        qty = q;
    }

    public Integer getQty(){
        return qty;
    }

    public void setPrice(Double p){
        price = p;
    }

    public Double getPrice(){
        return price;
    }

    public void print(){
        System.out.println("{'Price' : " + price.toString() + ", 'qty' : " + qty.toString() + "}");
    }
}