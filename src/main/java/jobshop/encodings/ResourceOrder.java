package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Arrays;


public class ResourceOrder extends Encoding {


    public final Task[][] rep;

    public ResourceOrder(Instance instance) {
        super(instance);
        rep = new Task[instance.numMachines][instance.numJobs];

        for (int i = 0; i < this.rep.length; i++) {
            for (int j = 0; j < this.rep[i].length; j++) {
                rep[i][j] = new Task(-1, -1);
            }
        }
    }

    public boolean finished (int[]  nextTask) {
        for(int i : nextTask){
            if ( i<instance.numTasks){
                return false;
            }
        }
        return true;
    }

    public Task nextCouple(int res, Task curCouple){
        for(int i=0;i<(instance.numJobs-1);i++){
            if(rep[res][i].equals(curCouple)){
                return rep[res][i+1];
            }
        }
        Task nullTask = new Task(-1,-1);
        return nullTask;
    }
    @Override
    public Schedule toSchedule() {
        // time at which each machine is going to be freed
        int[] nextFreeTimeResource = new int[instance.numMachines];

        // for eachS job, the first task that has not yet been scheduled
        int[] nextTask = new int[instance.numJobs];
        int[] nextFreeTimeJob = new int[instance.numJobs];
        for (int j =0;j<instance.numJobs;j++){
            nextTask[j]=0;
            nextFreeTimeJob[j]=0;
        }

        Task[] nextCouple = new Task[instance.numMachines];

        for(int r =0;r< instance.numMachines;r++){
            nextCouple[r]=rep[r][0];
            nextFreeTimeResource[r]=0;
        }


        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];
        int time =0;
        while (!finished(nextTask)){
            for(int job=0;job<instance.numJobs;job++){
                int task = nextTask[job];
                if (task <instance.numTasks){
                    int res =instance.machine(job, task);
                    Task cur = new Task(job, task);
                    //System.out.println("check cur "+ cur.toString()+" equals "+nextCouple[res].toString());
                    if (nextCouple[res].equals(cur)){
                        time=Math.max(nextFreeTimeResource[res], nextFreeTimeJob[job]);
                        startTimes[job][task]=time;
                        nextFreeTimeResource[res]=time + instance.duration(job, task);
                        nextFreeTimeJob[job]=time + instance.duration(job, task);
                        nextTask[job]+=1;
                        nextCouple[res]=nextCouple(res,cur);
                    }
                }
            }
        }

        return new Schedule(instance, startTimes);

    }
}