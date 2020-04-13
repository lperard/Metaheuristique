package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

            ResourceOrder ro = new ResourceOrder(instance);
            ro.rep[0][0]=new Task(0,0);
            ro.rep[0][1]=new Task(1,1);
            ro.rep[1][0]=new Task(1,0);
            ro.rep[1][1]=new Task(0,1);
            ro.rep[2][0]=new Task(0,2);
            ro.rep[2][1]=new Task(1,2);


            System.out.println("\nENCODING: " + ro.toString());
            System.out.println("\nENCODING: " + enc);

            Schedule sched = enc.toSchedule();
            // by implementing the toString() method
            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            System.out.println("Times: " + sched.toString());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
