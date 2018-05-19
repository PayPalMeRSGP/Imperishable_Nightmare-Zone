package ScriptClasses;

import Nodes.CheatCaveNodes.MidDreamNode;
import ScriptClasses.Util.NoSuitableNodesException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class MarkovNodeExecutor {

    public interface ExecutableNode {
        boolean canExecute() throws InterruptedException;
        int executeNode() throws InterruptedException;
        boolean doConditionalTraverse(); //used by MarkovNodeExecutor to indicate whether a special node traversal is requested
    }

    private class NodeEdge {
        final ExecutableNode u; //source node
        final ExecutableNode v; //destination node
        final int edgeExecutionWeight; //how often do we randomly traverse to this node, higher = more frequent. Relative to edgeExecutionWeights of sibling nodes.
        //ex: if node A had outgoing edges with weights 2, 3, 5. Then edge with weight 2 will be executed 20% (because 2/(2+3+5) = 0.20) of the time, 3 -> 30%, and 5 -> 50%.

        NodeEdge(ExecutableNode u, ExecutableNode v, int edgeExecutionWeight) {
            this.u = u;
            this.v = v;
            this.edgeExecutionWeight = edgeExecutionWeight;
        }
    }

    private final HashMap<ExecutableNode, LinkedList<NodeEdge>> normalAdjMap; //These edges are traversed by default
    private final HashMap<ExecutableNode, LinkedList<NodeEdge>> conditionalAdjMap; //" " traversed if doConditionalTraverse() returns true
    private ExecutableNode current;

    public MarkovNodeExecutor(ExecutableNode startingNode){
        normalAdjMap = new HashMap<>();
        conditionalAdjMap= new HashMap<>();
        current = startingNode;
    }

    public void addNormalEdgeToNode(ExecutableNode u, ExecutableNode v, int edgeExecutionWeight){
        if(normalAdjMap.containsKey(u)){
            LinkedList<NodeEdge> edges = normalAdjMap.get(u);
            if(edges == null){
                edges = new LinkedList<>();

            }
            edges.add(new NodeEdge(u, v, edgeExecutionWeight));
            normalAdjMap.put(u, edges);
        }
        else{
            LinkedList<NodeEdge> edges = new LinkedList<>();
            edges.add(new NodeEdge(u, v, edgeExecutionWeight));
            normalAdjMap.put(u, edges);
        }
    }

    public void addCondEdgeToNode(ExecutableNode u, ExecutableNode v, int edgeExecutionWeight){
        if(conditionalAdjMap.containsKey(u)){
            LinkedList<NodeEdge> edges = conditionalAdjMap.get(u);
            if(edges == null){
                edges = new LinkedList<>();

            }
            edges.add(new NodeEdge(u, v, edgeExecutionWeight));
            conditionalAdjMap.put(u, edges);
        }
        else{
            LinkedList<NodeEdge> edges = new LinkedList<>();
            edges.add(new NodeEdge(u, v, edgeExecutionWeight));
            conditionalAdjMap.put(u, edges);
        }
    }

    public void deleteNormalEdgeForNode(ExecutableNode u, ExecutableNode v){
        if(normalAdjMap.containsKey(u)){
            LinkedList<NodeEdge> edges = normalAdjMap.get(u);
            edges.forEach(edge -> {
                if(edge.v == v){
                    edges.remove(edge);
                }
            });
        }
    }

    public void deleteCondEdgeForNode(ExecutableNode u, ExecutableNode v){
        if(conditionalAdjMap.containsKey(u)){
            LinkedList<NodeEdge> edges = conditionalAdjMap.get(u);
            edges.forEach(edge -> {
                if(edge.v == v){
                    edges.remove(edge);
                }
            });
        }
    }
    /*
    returns the sleeptime until the next onLoop call.
    inside onloop there should be a line such as:
    return executor.executeThenTraverse();
    where executor is an instance of this class

    sleep times returns are implemented inside the executeNode() method (which returns an int) in each ExecutableNode instance
     */
    public int executeThenTraverse() throws InterruptedException, NoSuitableNodesException {
        int onLoopSleepTime;
        if(current.canExecute()){
            onLoopSleepTime = current.executeNode();
            if(current.doConditionalTraverse()) {
                conditionalTraverse();
                if(current instanceof MidDreamNode)
                    ((MidDreamNode) current).resumeNode();
            }
            else
                normalTraverse();

        }
        else{
            /*
            if current canExecute returned false, try to execute all nodes until a nodes canExecute returns true.
            Throw NoSuitableNodesException if all nodes can not execute.
            */
            for(ExecutableNode node: normalAdjMap.keySet()){
                if(node.canExecute()){
                    current = node;
                    return executeThenTraverse();
                }
            }

            throw new NoSuitableNodesException("did not find a suitable node that can execute.");

        }
        return onLoopSleepTime;
    }

    private void normalTraverse(){
        if(current != null){
            LinkedList<NodeEdge> edges = normalAdjMap.get(current);
            if(edges == null){
                throw new NullPointerException("edges is null, likely this node was not added into the graph.");
            }
            if(edges.size() == 0){
                return; //if no outgoing edges, current does not get changed therefore the same node will be repeated.
            }

            // Algorithm for random percentage branching
            // https://stackoverflow.com/questions/45836397/coding-pattern-for-random-percentage-branching?noredirect=1&lq=1
            int combinedWeight = edges.stream().mapToInt(edge -> edge.edgeExecutionWeight).sum();
            int sum = 0;
            int roll = ThreadLocalRandom.current().nextInt(1, combinedWeight+1);
            NodeEdge selectedEdge = null;
            for(NodeEdge edge: edges){
                sum += edge.edgeExecutionWeight;
                if(sum >= roll){
                    selectedEdge = edge;
                    break;
                }
            }
            if(selectedEdge == null){
                selectedEdge = edges.getLast();
            }
            current = selectedEdge.v;
        }
    }

    private void conditionalTraverse(){
        if(current != null){
            LinkedList<NodeEdge> edges = conditionalAdjMap.get(current);
            if(edges.size() == 0){
                return;
            }
            int combinedWeight = edges.stream().mapToInt(edge -> edge.edgeExecutionWeight).sum();
            int sum = 0;
            int roll = ThreadLocalRandom.current().nextInt(1, combinedWeight+1);
            NodeEdge selectedEdge = null;
            for(NodeEdge edge: edges){
                sum += edge.edgeExecutionWeight;
                if(sum >= roll){
                    selectedEdge = edge;
                    break;
                }
            }
            if(selectedEdge == null){
                selectedEdge = edges.getLast();
            }
            current = selectedEdge.v;
        }
    }
}
