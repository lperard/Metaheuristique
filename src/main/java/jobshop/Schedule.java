package jobshop;


import java.util.Arrays;

public class Schedule {
    public final Instance pb;
    // start times of each job and task
    // times[j][i] is the start time of task (j,i) : i^th task of the j^th job
    final int[][] times;

    public Schedule(Instance pb, int[][] times) {
        this.pb = pb;
        this.times = new int[pb.numJobs][];
        for(int j = 0 ; j < pb.numJobs ; j++) {
            this.times[j] = Arrays.copyOf(times[j], pb.numTasks);
        }
    }

    public int startTime(int job, int task) {
        return times[job][task];
    }

    /** Returns true if this schedule is valid (no constraint is violated) */
    public boolean isValid() {
        for(int j = 0 ; j<pb.numJobs ; j++) {
            for(int t = 1 ; t<pb.numTasks ; t++) {
                if(startTime(j, t-1) + pb.duration(j, t-1) > startTime(j, t))
                    return false;
            }
            for(int t = 0 ; t<pb.numTasks ; t++) {
                if(startTime(j, t) < 0)
                    return false;
            }
        }

        for (int machine = 0 ; machine < pb.numMachines ; machine++) {
            for(int j1=0 ; j1<pb.numJobs ; j1++) {
                int t1 = pb.task_with_machine(j1, machine);
                for(int j2=j1+1 ; j2<pb.numJobs ; j2++) {
                    int t2 = pb.task_with_machine(j2, machine);

                    boolean t1_first = startTime(j1, t1) + pb.duration(j1, t1) <= startTime(j2, t2);
                    boolean t2_first = startTime(j2, t2) + pb.duration(j2, t2) <= startTime(j1, t1);

                    if(!t1_first && !t2_first)
                        return false;
                }
            }
        }

        return true;
    }

    public int makespan() {
        int max = -1;
        for(int j = 0 ; j<pb.numJobs ; j++) {
            max = Math.max(max, startTime(j, pb.numTasks-1) + pb.duration(j, pb.numTasks -1));
        }
        return max;
    }

    public Schedule copy() {
        return new Schedule(this.pb, this.times);
    }
}
