import java.util.List;
import java.util.Map;

public interface FactorPlan {

    public void setFactorName(String name);
//    public void setVariablesAndOutcomes(Map<String, List<String>> variablesAndOutcomes);
//    public void setProbabilities(List<Double> probabilities);
    public void setCpt(CPT cpt);
}
