public class Income extends Transaction {

    public Income(double amount) {
        super(amount);
    }

    @Override
    public String getType() {
        return "Income";
    }
}