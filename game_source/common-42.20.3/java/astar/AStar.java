/*
 * Decompiled with CFR 0.152.
 */
package astar;

import astar.IGoalNode;
import astar.ISearchNode;
import astar.datastructures.ClosedSet;
import astar.datastructures.ClosedSetHash;
import astar.datastructures.IClosedSet;
import astar.datastructures.IOpenSet;
import astar.datastructures.OpenSet;
import java.util.ArrayList;
import java.util.Comparator;

public class AStar {
    private final int verbose = 0;
    private int maxSteps = -1;
    private int numSearchSteps;
    public ISearchNode bestNodeAfterSearch;
    private final ArrayList<ISearchNode> successorNodes = new ArrayList();
    private final IOpenSet openSet = new OpenSet(new SearchNodeComparator());
    private final IClosedSet closedSetHash = new ClosedSetHash(new SearchNodeComparator());
    private final IClosedSet closedSetNoHash = new ClosedSet(new SearchNodeComparator());

    public ArrayList<ISearchNode> shortestPath(ISearchNode initialNode, IGoalNode goalNode) {
        ISearchNode endNode = this.search(initialNode, goalNode);
        if (endNode == null) {
            return null;
        }
        return AStar.path(endNode);
    }

    public ISearchNode search(ISearchNode initialNode, IGoalNode goalNode) {
        boolean implementsHash = initialNode.keyCode() != null;
        initialNode.setDepth(0);
        this.openSet.clear();
        this.openSet.add(initialNode);
        IClosedSet closedSet = implementsHash ? this.closedSetHash : this.closedSetNoHash;
        closedSet.clear();
        this.numSearchSteps = 0;
        while (this.openSet.size() > 0 && (this.maxSteps < 0 || this.numSearchSteps < this.maxSteps)) {
            ISearchNode currentNode = this.openSet.poll();
            if (goalNode.inGoal(currentNode)) {
                this.bestNodeAfterSearch = currentNode;
                return currentNode;
            }
            this.successorNodes.clear();
            currentNode.getSuccessors(this.successorNodes);
            for (int i = 0; i < this.successorNodes.size(); ++i) {
                boolean inOpenSet;
                ISearchNode successorNode = this.successorNodes.get(i);
                if (successorNode == currentNode.getParent() || closedSet.contains(successorNode)) continue;
                ISearchNode discSuccessorNode = this.openSet.getNode(successorNode);
                if (discSuccessorNode != null) {
                    successorNode = discSuccessorNode;
                    inOpenSet = true;
                } else {
                    inOpenSet = false;
                }
                double tentativeG = currentNode.g() + currentNode.c(successorNode);
                if (inOpenSet && tentativeG >= successorNode.g()) continue;
                successorNode.setParent(currentNode);
                successorNode.setDepth(currentNode.getDepth() + 1);
                if (inOpenSet) {
                    this.openSet.remove(successorNode);
                    successorNode.setG(tentativeG);
                    this.openSet.add(successorNode);
                    continue;
                }
                successorNode.setG(tentativeG);
                this.openSet.add(successorNode);
            }
            closedSet.add(currentNode);
            ++this.numSearchSteps;
        }
        this.bestNodeAfterSearch = closedSet.min();
        return null;
    }

    public static ArrayList<ISearchNode> path(ISearchNode node) {
        ArrayList<ISearchNode> path = new ArrayList<ISearchNode>();
        path.add(node);
        ISearchNode currentNode = node;
        while (currentNode.getParent() != null) {
            ISearchNode parent = currentNode.getParent();
            path.add(0, parent);
            if (path.size() > 5000) {
                throw new RuntimeException("circular path?");
            }
            currentNode = parent;
        }
        return path;
    }

    public int numSearchSteps() {
        return this.numSearchSteps;
    }

    public ISearchNode bestNodeAfterSearch() {
        return this.bestNodeAfterSearch;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    static class SearchNodeComparator
    implements Comparator<ISearchNode> {
        SearchNodeComparator() {
        }

        @Override
        public int compare(ISearchNode node1, ISearchNode node2) {
            return Double.compare(node1.f(), node2.f());
        }
    }
}

