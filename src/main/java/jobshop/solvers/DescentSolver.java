package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    public static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
        public String toString(){
            return "Block using machine :" + this.machine +", index of first task :" + this.firstTask + ", index of end task :" + this.lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swap with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }
        public String toString(){
            return this.machine + " " + this.t1 + " " + this.t2;
        }
        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order){
            Task memory = order.tasksByMachine[this.machine][this.t1];
            //System.out.println("Trying to swap " + this.t1 + " and " + this.t2);
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = memory;
            //System.out.println("VALID " + order.toSchedule().isValid());
        }

        public boolean equals(Swap s){
            /*System.out.println("About to print les machines");
            System.out.println(this.machine);
            System.out.println(s.machine);*/
            if(this.machine == s.machine){
                if(this.t1 == s.t1){
                    if(this.t2 == s.t2) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        //solution initiale
        GreedySolver gs = new GreedySolver(GreedySolver.Type.EST_LRPT);
        Result res = gs.solve(instance, 10000);
        ResourceOrder solution = new ResourceOrder(res.schedule);

        boolean foundBetter;
        //Tant que l'on trouve un voisin avec un meilleur makespan
        do {
            foundBetter = false;
            //A chaque début de boucle, on sauvegarde le meilleur actuel et on récupére sa durée
            ResourceOrder bestSolution = solution.copy();
            int bestSolutionMakeSpan = bestSolution.toSchedule().makespan();
            //On trouve tout les blocs de la situation actuelle
            List<Block> blocks = blocksOfCriticalPath(bestSolution);
            //Sur tous ces blocs, on génére les voisins
            List<Swap> neighborsOfBlock = new ArrayList<>();
            for (Block b : blocks) {
                neighborsOfBlock.addAll(neighbors(b));
            }

            boolean foundLocalBetter = false;
            int neighbormakespan = Integer.MAX_VALUE;

            for(Swap s : neighborsOfBlock){
                //Pour chaque swap, on test si on a trouver une meilleure solution
                ResourceOrder current = bestSolution.copy();
                s.applyOn(current);
                int durationOfSwapped = current.toSchedule().makespan();
                //Si on a trouvé une meilleure solution
                if (durationOfSwapped < bestSolutionMakeSpan){
                    neighbormakespan = durationOfSwapped;
                    bestSolution = current.copy();
                    foundLocalBetter = true;
                }
            }
            //Si on a trouvé mieux, et que cette solution est meilleure que le minimum global actuel
            if(foundLocalBetter && (neighbormakespan < bestSolutionMakeSpan)){
                solution = bestSolution.copy();
                foundBetter = true;
                //System.out.println("We found better");
            }
        } while (foundBetter) ;

        return new Result(instance, solution.toSchedule(), Result.ExitCause.Timeout);

    }

    /** Returns a list of all blocks of the critical path. */
    public static List<Block> blocksOfCriticalPath(ResourceOrder order) {
        Schedule sched = order.toSchedule() ;
        List<Task> path = sched.criticalPath() ;
        List<Block> blocks = new ArrayList<DescentSolver.Block>();

        int currentMachine = order.instance.machine(path.get(0)) ;
        int startCurrentBlock = 0;
        for (Task currentTask : path) {
            int index = path.indexOf(currentTask) ;
            if (order.instance.machine(currentTask) != currentMachine || index == path.size()-1) {
                int endCurrentBlock ;
                if (index == path.size()-1) {
                    endCurrentBlock = index ;
                    if (order.instance.machine(currentTask) != currentMachine) {
                        endCurrentBlock = index-1;
                    }
                } else {
                    endCurrentBlock = index-1;
                }

                if (endCurrentBlock > startCurrentBlock) {
                    List<Task> taskOfMachine = Arrays.asList(order.tasksByMachine[currentMachine]) ;
                    blocks.add(new Block(currentMachine, taskOfMachine.indexOf(path.get(startCurrentBlock)), taskOfMachine.indexOf(path.get(endCurrentBlock)))) ;
                }
                startCurrentBlock = index;
                currentMachine = order.instance.machine(currentTask) ;
            }
        }
        return blocks;
    }


    private static int findIndex(ResourceOrder res, Task t, int machineUsed) {
        for (int i = 0; i < res.instance.numJobs; i++){
            //System.out.println("Is "+t+" : " + res.tasksByMachine[machineUsed][i] + " ?" );
            if(res.tasksByMachine[machineUsed][i].equals(t)){
                //System.out.println("YES");
                return i;
            }
        }
        System.out.println("Couldn't find task : " + t);
        return -1;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    public static List<Swap> neighbors(Block block) {
        List<Swap> swapped = new ArrayList();
        //si le block est de longueur 2
        int blockLength = block.lastTask - block.firstTask;
        if (blockLength == 1) {
            Swap only = new Swap(block.machine, block.firstTask, block.lastTask);
            swapped.add(only);
        }
        //sinon, le bloc est plus grand
        else if ( blockLength > 1){
            /*System.out.println("Block length is " + (block.lastTask - block.firstTask) + " , trying to create 2 swaps :" + block.firstTask + " , " +  block.firstTask +1 );
            System.out.println(" and " + block.firstTask + " , " +  block.firstTask +1 );*/
            Swap first = new Swap(block.machine, block.firstTask, block.firstTask + 1);
            Swap second = new Swap(block.machine, block.lastTask -1 , block.lastTask );
            swapped.add(first);
            swapped.add(second);
        }
        else {
            //System.out.println("Block length seems wrong : " + blockLength);
        }
        return swapped;
    }

}
