package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabooSolver extends DescentSolver {

    private int maxIter;
    private int dureeTaboo;

    private int taille;
    public TabooSolver (int maxIter, int dureeTaboo){
        super();
        this.maxIter = maxIter;
        this.dureeTaboo = dureeTaboo;
    }

    public Result solve(Instance instance, long deadline) {
        GreedySolver gs = new GreedySolver(GreedySolver.Type.EST_LRPT);
        ResourceOrder solution = new ResourceOrder(gs.solve(instance, System.currentTimeMillis() + 10).schedule);

        this.taille = instance.numJobs * instance.numTasks;
        int[][] tableauTaboo = new int[taille][taille];
        for(int i = 0; i<taille; i++){
            Arrays.fill(tableauTaboo[i], 0);
        }
        int compteur = 0;

        ResourceOrder bestNeighbor = solution.copy();
        while(compteur < maxIter){

            int bestNeighborMakespan = Integer.MAX_VALUE;
            int bestSolutionMakeSpan = solution.toSchedule().makespan();
            compteur++;

            List<Block> blocks = blocksOfCriticalPath(bestNeighbor);
            List<Swap> neighborsOfBlock = new ArrayList<>();
            for (Block b : blocks) {
                List<Swap> n = neighbors(b);
                neighborsOfBlock.addAll(n);
            }

            Swap bestSwap = null;
            for(Swap s : neighborsOfBlock){
                if(isTaboo(tableauTaboo, s, compteur)){
                    ResourceOrder current = bestNeighbor.copy();
                    s.applyOn(current);
                    Schedule sched = current.toSchedule();
                        int durationOfSwapped = sched.makespan();
                        if (durationOfSwapped <= bestNeighborMakespan){
                            //System.out.println("Found a better local solution : " + durationOfSwapped + " , instead of : " + bestNeighborMakespan);
                            bestSwap = s;
                            bestNeighborMakespan = durationOfSwapped;
                            bestNeighbor = current.copy();
                            if (durationOfSwapped <= bestSolutionMakeSpan){
                                //System.out.println("Found a better global solution : " + durationOfSwapped + " , instead of : " + bestSolutionMakeSpan);
                                solution = current.copy();
                            }
                        }
                }
            }
            //System.out.println(compteur+" : another taboo round, global : " + bestSolutionMakeSpan + " , local : " + bestNeighborMakespan);
            if(bestSwap!=null){
                addTabooSwaps(tableauTaboo, neighborsOfBlock, bestSwap, compteur);
            }


        }
        return new Result(instance, solution.toSchedule(), Result.ExitCause.Timeout);
    }

    private void addTabooSwaps (int[][] tabooArray, List<Swap> swaps, Swap s, int compteur){
        if (swaps.size() > 1){
            for (Swap considered : swaps) {
                if (s != null){
                    s.toString();
                    if (!considered.equals(s)){
                        int cell1 = s.machine * s.t1;
                        int cell2 = s.machine * s.t2;
                        tabooArray[cell1][cell2] = tabooArray[cell1][cell2] + compteur + dureeTaboo;
                    }
                }
            }
        }
    }

    //returns false if the move can't be done
    private Boolean isTaboo (int[][] tabooArray, Swap s, int compteur) {
        int cell1 = s.machine * s.t1;
        int cell2 = s.machine * s.t2;
        if(compteur > tabooArray[cell1][cell2]){
            //System.out.println("Not taboo");
            return true;
        }
        else{
            //System.out.println("Taboo!");
            return false;
        }
    }

}
