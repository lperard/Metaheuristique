package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;

public class GreedySolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {

        ResourceOrder sol = new ResourceOrder(instance);
        int[] nextTask = new int[instance.numJobs];
        for (int i=0; i<instance.numJobs; i++) {
            nextTask[i]=0;
        }
        



        /*for(int t = 0 ; t<instance.numTasks ; t++) {
            for(int j = 0 ; j<instance.numJobs ; j++) {
                sol.jobs[sol.nextToSet++] = j;
            }
        }*/

        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }
}
