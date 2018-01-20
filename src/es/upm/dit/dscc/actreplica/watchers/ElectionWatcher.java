package es.upm.dit.dscc.actreplica.watchers;

import es.upm.dit.dscc.actreplica.Bank;
import es.upm.dit.dscc.actreplica.node_managers.ElectionManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by Stefano on 20/01/2018.
 */
public class ElectionWatcher implements Watcher {

    Bank bank;
    ElectionManager electionManager;

    public ElectionWatcher(Bank bankInstance, ElectionManager electionManager){
        this.bank = bankInstance;
        this.electionManager = electionManager;
    }

    @Override
    public void process(WatchedEvent event) {
        System.err.println("ElectionWatcher process event ---> " + event.getType());

        try {
            electionManager.leaderElection();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
