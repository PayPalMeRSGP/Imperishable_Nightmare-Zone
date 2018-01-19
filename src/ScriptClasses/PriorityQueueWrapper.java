package ScriptClasses;

import Nodes.ExecutableNode;
import Nodes.AFKNode;
import Nodes.PrepNode;

import java.util.PriorityQueue;

public class PriorityQueueWrapper {
    private PriorityQueue<ExecutableNode> pq;

    public PriorityQueueWrapper(){
        this.pq = new PriorityQueue<>();
        pq.add(PrepNode.getPrepNodeInstance());
        pq.add(AFKNode.getMainAFKNodeInstance());
    }

    public int executeTopNode() throws InterruptedException {
        ExecutableNode nextNode = this.pq.peek();
        PublicStaticFinalConstants.hostScriptReference.log("executing: " + nextNode.toString());
        if(nextNode instanceof PrepNode){

            nextNode.setKey(1000);

            pq.remove(nextNode);
            pq.add(nextNode);
            return nextNode.executeNodeAction();
        }
        else if(nextNode instanceof AFKNode){
            return nextNode.executeNodeAction();
        }

        return 0;
    }

    private void debugPQ(){
        PriorityQueue<ExecutableNode> pqCopy = new PriorityQueue<>(pq);
        while(!pqCopy.isEmpty()){
            ExecutableNode node = pqCopy.poll();
            PublicStaticFinalConstants.hostScriptReference.log(node.toString());
        }
        PublicStaticFinalConstants.hostScriptReference.log("--------------------------------------------------------");
    }
}
