package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.Arrays;

public class GreedySolver implements Solver {

    private ResourceOrder res;
    public Type type;
    public GreedySolver (Type type) {
        this.type = type;
    }

    public ResourceOrder getResourceOrder () {
        return this.res;
    }
    @Override
    public Result solve(Instance instance, long deadline) {
        ResourceOrder sol = new ResourceOrder(instance);

        //Pour chaque machine, donne le moment où elle est disponible
        int[] machineAvailable = new int[instance.numMachines];
        //Pour chaque job, donne le moment où la tâche d'avant se termine
        int[] jobTime = new int[instance.numJobs];
        //Tableau avec le numéro de la prochaine tâche pour chaque job
        int[] realisable = new int[instance.numJobs];

        while(!Done(instance, realisable)){
            Task found = new Task(-1,-1);
            if (type == Type.SPT) {
                found = findShortest(instance, realisable, machineAvailable, jobTime);
            }
            else if (type == Type.EST_SPT) {
                Task[] selected = makeEST(instance, realisable, machineAvailable, jobTime);
                found = findActualShortest(instance, selected);
            }
            else if (type == Type.LRPT) {
                Task[] tasks = new Task[instance.numJobs];
                for (int i = 0; i < instance.numJobs; i++){
                    tasks[i] = new Task(i,realisable[i]);
                }
                found = longestJobFromList(instance, tasks, realisable);
            }
            else if (type == Type.EST_LRPT) {
                Task[] tasks = makeEST(instance, realisable, machineAvailable, jobTime);
                found = longestJobFromList(instance, tasks, realisable);
            }
            else {
                System.out.println("Can't recognize the type of greedy solver asked");
            }
            //on remplit la case de la machine qu'utilise la tâche, au bon emplacement
            sol.tasksByMachine[instance.machine(found)][sol.nextFreeSlot[instance.machine(found)]] = found;
            //on indique que l'emplacement disponible pour la machine utilisée est incrémenté
            sol.nextFreeSlot[instance.machine(found)]++;
            //pour ce job, il faut faire la tâche suivante
            realisable[found.job]++;

            int endTaskTime = instance.duration(found) + Math.max(machineAvailable[instance.machine(found.job, found.task)], jobTime[found.job]);
            //la machine est disponible après la durée de la tâche
            machineAvailable[instance.machine(found)] = endTaskTime;
            //la prochaine tâche du job est faisable après que cette tâche se soit terminée
            jobTime[found.job] = endTaskTime;
        }
        this.res = sol;
        Schedule greedy = sol.toSchedule();

        return new Result(instance, greedy, Result.ExitCause.Timeout);
    }

    private Task[] makeEST (Instance instance, int[] realisable, int[] machineAvailable, int[] jobTime) {
        Task[] tasksEST = new Task[instance.numJobs];
        int minStartTime = Integer.MAX_VALUE;
        for (int job = 0; job < instance.numJobs; job++){
            if (realisable[job] != instance.numTasks) {
                int startTime = Math.max(machineAvailable[instance.machine(job, realisable[job])], jobTime[job]);
                if (startTime < minStartTime) {
                    minStartTime = startTime;
                }
            }
        }
        for (int job = 0; job < instance.numJobs; job++){
            if (realisable[job] != instance.numTasks) {
                int startTime = Math.max(machineAvailable[instance.machine(job, realisable[job])], jobTime[job]);
                if (startTime == minStartTime){
                    tasksEST[job] = new Task(job,realisable[job]);
                }
            }
        }
        return tasksEST;
    }

    private Task longestJobFromList (Instance instance, Task[] selectable, int[] realisable){
        int longestJob = 0;
        int longestTime = 0;
        for(Task t : selectable){
            int durationJobRemaining = 0;
            if (t != null) {
                int job = t.job;
                int currentTask = realisable[job];
                for (int i = currentTask; i < instance.numTasks; i++) {
                    durationJobRemaining += instance.duration(job, i);
                }
                if (durationJobRemaining > longestTime) {
                    longestTime = durationJobRemaining;
                    longestJob = job;
                }
            }
        }
        Task found = selectable[longestJob];
        return found;
    }

    /*private int longestJobRemaining(Instance instance, int[] realisable){
        int longestJob = 0;

        for (int job = 0; job < instance.numJobs; job++){
            int timeRemaining = 0;
            for (int remain = realisable[job]; remain != instance.numTasks; remain++){
            timeRemaining += instance.duration(job, realisable[remain]);
            }
            if (timeRemaining > longestJob) {
                longestJob = job;
            }
        }
        return longestJob;
    }*/

    private Task findActualShortest (Instance instance, Task[] selected) {
        Task shortest = new Task(-1,-1);
        int shortestDuration = Integer.MAX_VALUE;
        for (int job = 0; job < instance.numJobs; job++) {
            if(selected[job] != null) {
                int duration = instance.duration(selected[job]);
                if(duration < shortestDuration){
                    shortest = selected[job];
                    shortestDuration = duration;
                }
            }
        }
        return shortest;
    }

    /*private Task actualLongestRemaining(Instance instance, int[] realisable, int[] machineAvailable, int[] jobTime) {
        Task[] selected = new Task[instance.numJobs];
        if (this.type == Type.EST_LRPT) {
            selected = makeEST(instance, realisable, machineAvailable, jobTime);
        }
        else if (this.type == Type.LRPT) {
            for (int job = 0; job < instance.numJobs; job++) {
                selected[job] = new Task(job, realisable[job]);
            }
        }
        Task longest = new Task(-1,-1);
        int longestDuration = Integer.MIN_VALUE;
        for (int job = 0; job < instance.numJobs; job++) {
            if(selected[job] != null) {
                int duration = instance.duration(selected[job]);
                if(longestDuration <= duration) {
                    longest = selected[job];
                    longestDuration = duration;
                }
            }
        }
        return longest;
    }*/

    private Task findShortest (Instance instance, int[] nextTask, int[] machineAvailable, int[] jobTime) {

        Task shortest = new Task(-1,-1);
        int lowestStartingTime = Integer.MAX_VALUE;
        int shortestDuration = Integer.MAX_VALUE;
        for (int job = 0; job < instance.numJobs; job++){

            //test pour savoir si on a pas déjà executer toutes les tâches du job
            if(nextTask[job] != instance.numTasks) {
                //le moment de démarrage de cette tâche est le max entre le moment de dispo de la machine et de la fin de la tâche précédente
                int startingTimeTask = Math.max(machineAvailable[instance.machine(job, nextTask[job])], jobTime[job]);
                if (startingTimeTask <= lowestStartingTime){
                    //System.out.println("Found an earlier starting task : " + startingTimeTask +", instead of :" + lowestStartingTime);
                    if ( (instance.duration(job, nextTask[job]) < shortestDuration)) {
                        //System.out.println("Found an earlier starting task :"+ startingTimeTask + ", rather than " + lowestStartingTime);
                        shortestDuration = instance.duration(job, nextTask[job]);
                        shortest = new Task(job, nextTask[job]);
                        lowestStartingTime = startingTimeTask;
                    }
                }
            }
        }
        if (shortest.job == -1 || shortest.task == -1){
            System.out.println("DING DONG SOMETHING WRONG");
        }
        return shortest;
    }

    private Task longestRemaining (Instance instance, int[] nextTask, int[] machineAvailable, int[] jobTime) {
        int longestDuration = Integer.MIN_VALUE;
        Task longest = new Task(-1,-1);
        int lowestStartingTime = Integer.MAX_VALUE;

        for (int job = 0; job < instance.numJobs; job++){
            if (nextTask[job] != instance.numTasks) {
                int startingTimeTask = Math.max(machineAvailable[instance.machine(job, nextTask[job])], jobTime[job]);
                if (startingTimeTask <= lowestStartingTime){
                    if (  (instance.duration(job, nextTask[job]) > longestDuration)) {
                        longestDuration = instance.duration(job, nextTask[job]);
                        longest = new Task(job, nextTask[job]);
                        lowestStartingTime = startingTimeTask;
                    }
                }

            }
            //System.out.println("job : " + job +", nextTask for that job : " + nextTask[job]);

        }
        if (longest.job == -1 || longest.task == -1){
            System.out.println("DING DONG SOMETHING WRONG");
        }
        return longest;
    }
    private Boolean Done(Instance instance, int[] nextTask){
        Boolean end = true;
        //pour chaque job
        for(int job = 0; job < instance.numJobs; job++){
            //on teste si on a réaliser toutes ses tâches
            //Si, pour un job, on est pas arrivé au bout, Done est faux
            if(nextTask[job] != (instance.numTasks)){
                end = false;
            }
        }
        return end;
    }

    public enum Type {
        SPT,
        LRPT,
        EST_SPT,
        EST_LRPT;
    }
}

