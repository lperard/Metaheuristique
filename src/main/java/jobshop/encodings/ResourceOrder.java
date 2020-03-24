package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

public class ResourceOrder extends Encoding {
    public Task[][] representation;
    public Instance pb;
    public Schedule sol;

    public ResourceOrder (Instance pb) {
        super(pb);
        this.representation = new Task[this.pb.numMachines][this.pb.numJobs];
        for (int i = 0; i < this.pb.numMachines; i++) {
            for (int j = 0; j < this.pb.numJobs; j++) {
                representation[i][j] = new Task(-1,-1);
            }
        }

    }

    public Schedule toSchedule() {






        return null;
    }
}
