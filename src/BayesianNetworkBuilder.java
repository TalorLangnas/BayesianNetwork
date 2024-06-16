import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BayesianNetworkBuilder {
    public static BayesianNetwork buildNetworkFromXML(String xmlFilePath) {
        BayesianNetwork network = new BayesianNetwork();
        try {
            // DocumentBuilderFactory is used to create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // DocumentBuilder is used to parse the XML file
            DocumentBuilder builder = factory.newDocumentBuilder();
            // Parse the XML file
            Document document = builder.parse(new File(xmlFilePath));
            // Get the root element of the XML document
//            document.getDocumentElement().normalize();
            Element root = document.getDocumentElement();
            System.out.println("Root element: " + root.getNodeName());
            // This part is to build the nodes of the network:
            // Get all the elements with the tag name "VARIABLE"
            NodeList varList = document.getElementsByTagName("VARIABLE");
            System.out.println("total nodes: " + varList.getLength());

            for (int i = 0; i < varList.getLength(); i++) {
                Node varNode = varList.item(i);
                if (varNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element varElement = (Element) varNode;
                    // Get the name of the node
                    String nodeName = varElement.getElementsByTagName("NAME").item(0).getTextContent();
                    NodeList outcomes = varElement.getElementsByTagName("OUTCOME");
                    // Get the outcomes of the node
                    List<String> nodeOutcomes = new ArrayList<>();
                    for (int j = 0; j < outcomes.getLength(); j++) {
                        nodeOutcomes.add(outcomes.item(j).getTextContent());
                    }
                    // Add the node to the nodes list of the network
                    NetNode newNode = new NetNode(nodeName);
                    newNode.setOutcomes(nodeOutcomes);
                    network.addNode(newNode);
                }
            }

            // This part is set up the nodes relations (arcs) and CPT table
            NodeList defList = document.getElementsByTagName("DEFINITION");
            for (int i = 0; i < defList.getLength(); i++) {
                Node defNode = defList.item(i);
                if (defNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element defElement = (Element) defNode;
                    // Get the name of the node
                    String childNodeName = defElement.getElementsByTagName("FOR").item(0).getTextContent();
                    NetNode childNode = network.getNode(childNodeName);
                    CPT cpt = new CPT();
                    // option 1: define private Map<String, List<String>> variablesAndOutcomes of the "FOR" node
                    // option 2: create function Factor.addVariableAndOutcomes(String nodeName, List<String> outcomes)
                    // which appends the nodeName and its outcomes to the Factor object VariableAndOutcomes Map
                    // Get the parents of the node
                    NodeList parents = defElement.getElementsByTagName("GIVEN");
                    for (int j = 0; j < parents.getLength(); j++) {
                        String parentName = parents.item(j).getTextContent();
                        NetNode parentNode = network.getNode(parentName);
                        // add child to parent Node adjacency list
                        network.addNodeToAdjacencyList(parentName, network.getNode(childNodeName));
                        // add child to parent's children list
                        parentNode.addChild(childNode);
                        // add the parent to parents list of child BbNode
                        childNode.addParent(parentNode);
                        // Get the parentNode outcomes list
                        // option 1: add to the Map key: parentName, value: parentNode outcomes
                        // option 2: use the function from option 2
                        cpt.addVariablesAndOutcomes(parentName, parentNode.getOutcomes());
                    }
                    // option 1: add the child to the Map
                    // option 2: use the function from option 2
                    cpt.addVariablesAndOutcomes(childNodeName, childNode.getOutcomes());
                    // Get the CPT of the node
                    // Reverse the order of the cpt items
                    NodeList probabilities = defElement.getElementsByTagName("TABLE");
                    String probabilitiesContent = probabilities.item(0).getTextContent();
                    cpt.setProbabilities(probabilitiesContent);
                    cpt.buildCpt();
                    childNode.setCPT(cpt);
//                    // print CPT
//                    System.out.println("CPT for node: " + childNodeName);
//                    System.out.println(cpt.toString());
                    // create Factor function that takes this Probabilities string and creates the CPT
                    // the Factor that applies all this function is data member of the Child node
                }
//                System.out.println();

            }
//            System.out.println("------------- printing network");
//            network.printNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return network;
    }
}


