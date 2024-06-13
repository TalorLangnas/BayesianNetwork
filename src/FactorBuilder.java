import java.util.List;
import java.util.Map;

public interface FactorBuilder {
    public void buildFactorName(String name);

    public void buildVariablesAndOutcomes();
    public void addVariablesAndOutcomes(String nodeName, List<String> outcomes);
    public void buildProbabilities(String probabilities);
    public void buildCpt();
    public Factor getFactor();
}
