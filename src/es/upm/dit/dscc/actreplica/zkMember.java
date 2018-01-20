package es.upm.dit.dscc.actreplica;

import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class zkMember implements Watcher{

	public static String rootMembers = "/members";
	private static String aMember = "/member-";
	private String myId;

	private ZooKeeper zk;
	
	public zkMember(ZooKeeper zk) {

		this.zk = zk;

		// Add the process to the members in zookeeper
		if (zk != null) {
			// Create a folder for members and include this process/server
			try {
				// Create a folder, if it is not created
				String response = new String();
				Stat s = zk.exists(rootMembers, watcherMember);
				if (s == null) {
					// Created the znode, if it is not created.
					response = zk.create(rootMembers, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//					System.out.println(response);
				}

				// Create a znode for registering as member and get my id
				myId = zk.create(rootMembers + aMember, new byte[0], 
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

				myId = myId.replace(rootMembers + "/", "");

				List<String> list = zk.getChildren(rootMembers, watcherMember, s); //this, s);
//				System.out.println("Created znode nember id:"+ myId );
				printListMembers(list);
			} catch (KeeperException e) {
//				System.out.println("The session with Zookeeper failes. Closing");
				return;
			} catch (InterruptedException e) {
//				System.out.println("InterruptedException raised");
			}

		}
	}

	private Watcher  watcherMember = new Watcher() {
		public void process(WatchedEvent event) {
//			System.out.println("------------------Watcher Member------------------\n");
			try {
//				System.out.println("        ¡¡Update!!");
				List<String> list = zk.getChildren(rootMembers,  watcherMember); //this);
				printListMembers(list);
			} catch (Exception e) {
//				System.out.println("Exception: watcherMember");
				System.err.println(e.getMessage());
			}
		}
	};
	
	@Override
	public void process(WatchedEvent event) {
		try {
			//System.out.println("!!!!!!" + event.toString());
			List<String> list = zk.getChildren(rootMembers, watcherMember); //this);
			printListMembers(list);
		} catch (Exception e) {
//			System.out.println("Error in project");
		}
	}
	
	private void printListMembers (List<String> list) {
//		System.out.println("Remaining # members:" + list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");				
		}
//		System.out.println();

	}
}
