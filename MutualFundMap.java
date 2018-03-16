import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

class MutualFundElement {
    private String stockName;
    private Double percentage;
    public MutualFundElement(String stockName, Double percentage){
        this.stockName = stockName;
        this.percentage = percentage;
    }
    public String getStockName() {
        return stockName;
    }
    public Double getPercentage() {
        return percentage;
    }
}

class MutualFund implements Iterable<MutualFundElement> {
    public ArrayList<MutualFundElement> stockList = new ArrayList<MutualFundElement>();
    public void add(String stockName, Double percentage) {
        stockList.add(new MutualFundElement(stockName, percentage));
    }

    @Override
    public Iterator<MutualFundElement> iterator()
    {
        return stockList.iterator();
    }
}

public class MutualFundMap {
    public static HashMap<String, MutualFund> mutualFundMap = new HashMap<String, MutualFund>();
    public MutualFundMap () {
        MutualFund mf1 = new MutualFund();
        mf1.add("Deutsche Bank", 0.2);
        mf1.add("CREDIT AGRICOLE", 0.2);
        mf1.add("SOCIETE GENERALE", 0.1);
        mf1.add("American Express", 0.2);
        mf1.add("Goldman Sachs", 0.1);
        mf1.add("JPMorgan Chase", 0.15);
        mf1.add("Nomura Holdings, Inc.", 0.05);
        mutualFundMap.put("Mutal_Fund_Banking_1", mf1);

        MutualFund mf2 = new MutualFund();
        mf2.add("Petrobras", 0.15);
        mf2.add("BP PLC", 0.15);
        mf2.add("TOTAL", 0.4);
        mf2.add("ExxonMobil", 0.3);
        mutualFundMap.put("Mutal_Fund_Energy_1", mf2);

        MutualFund mf3 = new MutualFund();
        mf3.add("Swire Pacific Limited", 0.15);
        mf3.add("Softbank Corp.", 0.35);
        mf3.add("Sky PLC", 0.4);
        mf3.add("Deutsche Lufthansa", 0.1);
        mutualFundMap.put("Mutal_Fund_Diversified_1", mf3);
    }

    public MutualFund get(String mutralFundName) {
        return mutualFundMap.get(mutralFundName);
    }
}