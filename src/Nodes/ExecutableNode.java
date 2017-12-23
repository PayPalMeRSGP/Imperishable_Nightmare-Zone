package Nodes;

import org.osbot.rs07.script.Script;

public interface ExecutableNode {
    int executeNodeAction() throws InterruptedException;
    void resetKey();
    void setKey(int key);
    int getKey();
    String toString();

}
