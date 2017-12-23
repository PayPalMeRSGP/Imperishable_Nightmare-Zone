package ScriptClasses;

import Nodes.ExecutableNode;
import Nodes.MainAFKNode;
import Nodes.PrepNode;

import java.util.PriorityQueue;

public class PriorityQueueWrapper {
    private PriorityQueue<ExecutableNode> pq;

    public PriorityQueueWrapper(){
        this.pq = new PriorityQueue<>();
        pq.add(PrepNode.getPrepNodeInstance());
        pq.add(MainAFKNode.getMainAFKNodeInstance());
    }

    public int executeTopNode() throws InterruptedException {
        ExecutableNode nextNode = this.pq.peek();

        if(nextNode instanceof PrepNode){
            nextNode.executeNodeAction();
            nextNode.setKey(1000);
        }
        else if(nextNode instanceof MainAFKNode){
            nextNode.executeNodeAction();
        }

        return 0;
    }

    private void debugPQ(){
        PriorityQueue<ExecutableNode> pqCopy = new PriorityQueue<>(pq);
        while(!pqCopy.isEmpty()){
            ExecutableNode node = pqCopy.poll();
            ConstantsAndStatics.hostScriptReference.log(node.toString());
        }
        ConstantsAndStatics.hostScriptReference.log("--------------------------------------------------------");
    }
}
