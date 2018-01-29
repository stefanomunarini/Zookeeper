package es.upm.dit.dscc.actreplica.watchers;

import es.upm.dit.dscc.actreplica.Bank;
import es.upm.dit.dscc.actreplica.node_managers.ElectionManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * A watcher class for the /election znode.
 * This watcher is triggered when the leader node fails
 * or exits. A new election is then triggered.
 */
public class ElectionWatcher implements Watcher {

    private ElectionManager electionManager;

    public ElectionWatcher(ElectionManager electionManager){
        this.electionManager = electionManager;
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            electionManager.leaderElection();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
