package es.upm.dit.dscc.actreplica.node_managers;

import es.upm.dit.dscc.actreplica.Bank;
import es.upm.dit.dscc.actreplica.utils.NodeUtils;
import es.upm.dit.dscc.actreplica.watchers.ElectionWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Stefano on 20/01/2018.
 */
public class ElectionManager {

    private ZooKeeper zk;
    private Bank bank;

    public static String rootElection = "/election";

    public ElectionManager(ZooKeeper zkInstance, Bank bankInstance){
        this.zk = zkInstance;
        this.bank = bankInstance;
    }

    public String createElectionNode() throws KeeperException, InterruptedException {

        NodeUtils.znodeExistsOrCreate(zk, rootElection, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        return NodeUtils.znodeExistsOrCreate(zk, rootElection + "/n_", new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void leaderElection() throws KeeperException, InterruptedException {

        List<String> nodes = zk.getChildren(rootElection, false);
        int r = new Random().nextInt(100);
        // Loop for rand iterations
        // to wait that a few nodes join
        for (int i = 0; i < r; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {

            }
        }

        Collections.sort(nodes);
        String leader = nodes.get(0);
        this.bank.setLeader(leader);
        System.out.println("Leader name: " + leader);
        if(leader.equals(this.bank.getElectionNodeName().replace(rootElection + "/", ""))){
            this.bank.setIsLeader(true);
            System.out.println("****You are the leader****");
        } else {
            this.bank.setIsLeader(false);
            System.out.println("The process " + leader + " is the leader");
            listenForLeaderNode(leader);
        }
    }

    private void listenForLeaderNode(String leaderNode){
        ElectionWatcher electionWatcher = new ElectionWatcher(this);
        try {
//            Thread.sleep(1000);
            zk.exists(rootElection + "/" + leaderNode, electionWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
