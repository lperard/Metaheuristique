package jobshop;

import jobshop.solvers.GreedySolver;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.TabooSolver;
import sun.tools.jconsole.Tab;

import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/ft10"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            /*JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;*/


            //System.out.println("\nENCODING: " + enc);

            //System.out.println("SCHEDULE: " + sched);
            //System.out.println("VALID: " + sched.isValid());
            //System.out.println("MAKESPAN: " + sched.makespan());

            /*Result resBasic = new BasicSolver().solve(instance, Long.MAX_VALUE);
            Schedule schedBasic = resBasic.getSchedule();
            System.out.println("Solved using Basic Solver");
            System.out.println("VALID: " + schedBasic.isValid());
            System.out.println("MAKESPAN: " + schedBasic.makespan());
            */

            Result resGreedy = new GreedySolver(GreedySolver.Type.EST_LRPT).solve(instance, Long.MAX_VALUE);
            Schedule schedGreedy = resGreedy.getSchedule();
            System.out.println("Solved using Greedy Solver");
            System.out.println("VALID: " + schedGreedy.isValid());
            System.out.println("MAKESPAN: " + schedGreedy.makespan());

            System.out.println("");

            Result resDescent = new DescentSolver().solve(instance, 10000);
            Schedule schedDescent1 = resDescent.getSchedule();
            System.out.println("Solved using Descent Solver");
            System.out.println("VALID: " + schedDescent1.isValid());
            System.out.println("MAKESPAN: " + schedDescent1.makespan());

            System.out.println("");


            /*System.out.println("");
            Result resTaboo = new TabooSolver(10000,1).solve(instance, Long.MAX_VALUE);
            Schedule schedTaboo = resTaboo.getSchedule();
            System.out.println("Solved using Taboo Solver");
            System.out.println("VALID: " + schedTaboo.isValid());
            System.out.println("MAKESPAN: " + schedTaboo.makespan());*/

            int[] column = {1,2,5,10,50,100};
            int[] row = {10,50,100,200,500,1000};
            double[][] resultat = new double[row.length][column.length];

            for(int i = 0; i < row.length; i++){
                for(int j = 0; j < column.length; j++){
                    Result result = new TabooSolver(row[i], column[j]).solve(instance, 10000);
                    Schedule schedCase = result.getSchedule();
                    int ms = schedCase.makespan();
                    resultat[i][j] = ms;
                    System.out.println("Case " + i + " " + j + " = " + resultat[i][j]);
                }
            }




        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
