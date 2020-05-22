package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

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
        ResourceOrder solution = new ResourceOrder(gs.solve(instance, 1000).schedule);

        this.taille = instance.numJobs * instance.numTasks;
        int[][] tableauTaboo = new int[taille][taille];
        for(int i = 0; i<taille; i++){
            Arrays.fill(tableauTaboo[i], 0);
        }
        int compteur = 0;

        ResourceOrder bestNeighbor = solution.copy();
        while(compteur < maxIter){
            compteur++;

            int bestNeighborMakespan = Integer.MAX_VALUE;
            int bestSolutionMakeSpan = solution.toSchedule().makespan();

            List<Block> blocks = blocksOfCriticalPath(bestNeighbor);
            List<Swap> neighborsOfBlock = new ArrayList<>();
            for (Block b : blocks) {
                List<Swap> n = neighbors(b);
                neighborsOfBlock.addAll(n);
            }
            Swap bestSwap = null;
            for(Swap s : neighborsOfBlock){
                if(isTaboo(tableauTaboo, s, bestNeighbor, compteur)){
                    ResourceOrder current = bestNeighbor.copy();
                    s.applyOn(current);
                    Schedule sched = current.toSchedule();
                    if(sched == null){
                        break;
                    }
                        int durationOfSwapped = sched.makespan();
                        if (durationOfSwapped <= bestNeighborMakespan){
                            bestSwap = s;
                            bestNeighborMakespan = durationOfSwapped;
                            bestNeighbor = current.copy();
                            if (durationOfSwapped <= bestSolutionMakeSpan){
                                solution = current.copy();
                            }
                        }
                }
            }
            if(bestSwap!=null){
                addTabooSwaps(tableauTaboo, bestSwap, bestNeighbor, compteur);
            }


        }
        return new Result(instance, solution.toSchedule(), Result.ExitCause.Timeout);
    }

    private void addTabooSwaps (int[][] tabooArray, Swap s, ResourceOrder current, int compteur){
        Task task_1 = current.tasksByMachine[s.machine][s.t1];
        Task task_2 = current.tasksByMachine[s.machine][s.t2];
        tabooArray[task_1.job * current.instance.numTasks + task_1.task][task_2.job * current.instance.numTasks + task_2.task] = compteur + dureeTaboo;
    }

    //returns false if the move can't be done
    private Boolean isTaboo (int[][] tabooArray, Swap s, ResourceOrder current, int compteur) {
        Task task_1 = current.tasksByMachine[s.machine][s.t1];
        Task task_2 = current.tasksByMachine[s.machine][s.t2];
        if(compteur > tabooArray[task_1.job * current.instance.numTasks + task_1.task][task_2.job * current.instance.numTasks + task_2.task]){
            //System.out.println("Not taboo");
            return true;
        }
        else{
            //System.out.println("Taboo!");
            return false;
        }
    }

}
