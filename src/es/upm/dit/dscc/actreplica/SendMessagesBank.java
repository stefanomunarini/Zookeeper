package es.upm.dit.dscc.actreplica;

import es.upm.dit.dscc.actreplica.node_managers.ElectionManager;
import es.upm.dit.dscc.actreplica.node_managers.OperationsManager;
import es.upm.dit.dscc.actreplica.utils.NodeUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

public class SendMessagesBank implements SendMessages {

	private ZooKeeper zk;
	private Bank bank;

	public SendMessagesBank(ZooKeeper zk, Bank bank){
		this.zk = zk;
		this.bank = bank;
	}

	private void sendMessage(OperationBank operation, boolean isLeader) {
		if (isLeader){
			forwardOperationToFollowers(operation);
		} else {
			forwardOperationToLeader(operation);
		}
	}

	private void forwardOperationToLeader(OperationBank operation) {
		byte[] operationBytes = new byte[0];
		try {
			operationBytes = OperationBank.objToByte(operation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get leader operationNodeName which is stored as data in the electionNodeName of the leader
		String leaderElectionNodeName = ElectionManager.rootElection + "/" + this.bank.getLeader();
		try {
			String leaderOperationNodeName = NodeUtils.getLeaderOperationNodeName(zk, leaderElectionNodeName);
			zk.create(leaderOperationNodeName + "/", operationBytes,
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void forwardOperationToFollowers(OperationBank operation) {

		System.out.println("forwardOperationToFollowers");

		List<String> operationNodes = null;
		try {
			operationNodes = zk.getChildren(OperationsManager.rootOperations, false);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}

		byte[] operationBytes = new byte[0];
		try {
			operationBytes = OperationBank.objToByte(operation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Iterator iterator = operationNodes.iterator(); iterator.hasNext(); ) {
			String operation_node_id = (String) iterator.next();

			// Do not send the update to the leader (itself) again
			String leaderElectionNodeName = ElectionManager.rootElection + "/" + this.bank.getLeader();
			try {
				String leaderOperationNodeName = NodeUtils.getLeaderOperationNodeName(zk, leaderElectionNodeName);
				if (!operation_node_id.equals(leaderOperationNodeName)) {
					zk.create(OperationsManager.rootOperations + "/" + operation_node_id + "/", operationBytes,
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
				}
			} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendAdd(Client client, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.CREATE_CLIENT, client);
		if (isLeader) this.bank.handleReceiverMsg(operation);
		sendMessage(operation, isLeader);
	}

	public void sendUpdate(Client client, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.UPDATE_CLIENT, client);
		if (isLeader) this.bank.handleReceiverMsg(operation);
		sendMessage(operation, isLeader);
	}

	public void sendDelete(Integer accountNumber, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.DELETE_CLIENT, accountNumber);
		if (isLeader) this.bank.handleReceiverMsg(operation);
		sendMessage(operation, isLeader);
	}

	public void sendCreateBank (ClientDB clientDB, boolean isLeader) {

		// TODO only send to new connected server

		OperationBank operation = new OperationBank(OperationEnum.CREATE_BANK, clientDB);
		sendMessage(operation, isLeader);
	}
}
