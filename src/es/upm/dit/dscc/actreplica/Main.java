package es.upm.dit.dscc.actreplica;

import org.apache.zookeeper.KeeperException;

/**
 * Created by Stefano on 19/01/2018.
 */
public class Main {

    public static void main(String[] args) throws KeeperException, InterruptedException {
        new MainBank(args);
    }

}
