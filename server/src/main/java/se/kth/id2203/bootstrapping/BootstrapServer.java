/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.bootstrapping;

import com.google.common.collect.ImmutableSet;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.beb.BestEffortBroadcast;
import se.kth.id2203.broadcast.beb.TopologyResponse;
import se.kth.id2203.broadcast.epfd.EventuallyPerfectFailureDetector;
import se.kth.id2203.broadcast.epfd.Restore;
import se.kth.id2203.broadcast.epfd.Suspect;
import se.kth.id2203.broadcast.perfect_link.PL_Send;
import se.kth.id2203.kvstore.*;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.paxos.*;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

public class BootstrapServer extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(BootstrapServer.class);
    //******* Ports ******
    protected final Negative<Bootstrapping> boot = provides(Bootstrapping.class);
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    protected final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);
    protected final Positive<MultiPaxos> paxos = requires(MultiPaxos.class);
    //******* Fields ******
    public final static int PARTITION_COUNT = 2;
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    final int bootThreshold = config().getValue("id2203.project.bootThreshold", Integer.class);
    private State state = State.COLLECTING;
    private UUID timeoutId;
    private final Set<NetAddress> active = new HashSet<>();
    private final Set<NetAddress> ready = new HashSet<>();
    private final Map<Integer, Set<NetAddress>> partitions = new HashMap<>();
    private NodeAssignment initialAssignment = null;

    private List<Operation> holdbackQueue = new ArrayList<>();

    private Map<UUID, NetAddress> opClientMapping = new HashMap<>();

    //******* Handlers ******
    protected final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start e) {
            LOG.info("Starting bootstrap server on {}, waiting for {} nodes...", self, bootThreshold);
            long timeout = (config().getValue("id2203.project.keepAlivePeriod", Long.class) * 2);
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(timeout, timeout);
            spt.setTimeoutEvent(new BSTimeout(spt));
            trigger(spt, timer);
            timeoutId = spt.getTimeoutEvent().getTimeoutId();
            active.add(self);

        }
    };
    protected final Handler<BSTimeout> timeoutHandler = new Handler<BSTimeout>() {
        @Override
        public void handle(BSTimeout e) {
            if (state == State.COLLECTING) {
                LOG.info("{} hosts in active set.", active.size());
                if (active.size() >= bootThreshold) {
                    bootUp();
                }
            } else if (state == State.SEEDING) {
                LOG.info("{} hosts in ready set.", ready.size());
                if (ready.size() >= bootThreshold) {
                    LOG.info("Finished seeding. Bootstrapping complete.");
                    trigger(new Booted(initialAssignment), boot);
                    state = State.DONE;
                }
            } else if (state == State.DONE) {
                suicide();
            }
        }
    };
    protected final Handler<InitialAssignments> assignmentHandler = new Handler<InitialAssignments>() {
        @Override
        public void handle(InitialAssignments e) {
            LOG.info("Seeding assignments...");
            initialAssignment = e.assignment;
            for (NetAddress node : active) {
                trigger(new Message(self, node, new Boot(initialAssignment)), net);
            }
            ready.add(self);
        }
    };

    private int getPartitionKey(NetAddress adr){
        String str = adr.getIp().toString() + ":" + adr.getPort();
        return str.hashCode() % PARTITION_COUNT ;
    }

    private int getPartitionKeyOnMessage(Operation msg){
        return msg.key.hashCode() % PARTITION_COUNT;
    }

    private void addToPartition(int partitionKey, NetAddress adr){

        if(!partitions.containsKey(partitionKey)){
            partitions.put(partitionKey, new HashSet<NetAddress>());
        }

        Set<NetAddress> newPart = partitions.get(partitionKey);
        newPart.add(adr);
        partitions.put(partitionKey, newPart);
    }

    protected final ClassMatchedHandler<CheckIn, Message> checkinHandler = new ClassMatchedHandler<CheckIn, Message>() {

        @Override
        public void handle(CheckIn content, Message context) {
            active.add(context.getSource());

            int partitionKey = getPartitionKey(context.getSource());
            addToPartition(partitionKey, context.getSource());

            Set<NetAddress> p = partitions.get(partitionKey);

            for (NetAddress adr : p){
                trigger(new Message(self, adr, new TopologyResponse(p, partitionKey)), net);
            }

            trigger(new Message(self, self, new TopologyResponse(p, partitionKey)), net);

            System.out.println("Sending SSO");
            trigger(new Propose(new StopSignOperation(p, -1), partitionKey), paxos);

            if (holdbackQueue.size() > 0 && active.size() > 7) {
                System.out.println("Empty holdbackqueue " + holdbackQueue);
                Iterator iterator = holdbackQueue.iterator();
                List<Operation> toRemove = new ArrayList<>();
                while (iterator.hasNext()) {
                    Operation msg = (Operation) iterator.next();
                    System.out.println("Trigger it!");
                    trigger(new Propose(msg, getPartitionKeyOnMessage(msg)), paxos);
                    //trigger(new Message(self, getOneNodeFromPartition(msg) , msg), net);
                    toRemove.add(msg);
                }
                for (Operation t : toRemove) {
                    holdbackQueue.remove(t);
                }
            }
            
        }
    };
    protected final ClassMatchedHandler<Ready, Message> readyHandler = new ClassMatchedHandler<Ready, Message>() {
        @Override
        public void handle(Ready content, Message context) {
            ready.add(context.getSource());
        }
    };

    private void handleOperation(Operation operation, Message message) {
        System.out.println("Handle operation " + operation + " from " + message.getSource());
        opClientMapping.put(operation.id, message.getSource());
        if (active.size() < 7) {
            System.out.println("Put it in the holdback queue");
            holdbackQueue.add(operation);
        }
        else {
            trigger(new Message(self, getOneNodeFromPartition(operation) , operation), net);
        }
    }

    private void handleOperationResponse(OpResponse opResponse, Message message) {
        System.out.println("Received " + opResponse + " from " + message.getSource() + "\n" + opClientMapping.toString());
        if (opClientMapping.containsKey(opResponse.id)) {
            System.out.println("Send " + opResponse + " to " + opClientMapping.get(opResponse.id));
            trigger(new Message(self, opClientMapping.get(opResponse.id), opResponse), net);
            opClientMapping.remove(opResponse.id);
        }
    }

    protected final ClassMatchedHandler<PutOperation, Message> operationMessageClassMatchedHandler = new ClassMatchedHandler<PutOperation, Message>() {
        @Override
        public void handle(PutOperation operation, Message message) {
            handleOperation(operation, message);
        }
    };

    protected final ClassMatchedHandler<CasOperation, Message> casOperationMessageClassMatchedHandler = new ClassMatchedHandler<CasOperation, Message>() {
        @Override
        public void handle(CasOperation operation, Message message) {
            handleOperation(operation, message);
        }
    };

    protected final ClassMatchedHandler<GetOperation, Message> getOperationMessageClassMatchedHandler = new ClassMatchedHandler<GetOperation, Message>() {
        @Override
        public void handle(GetOperation operation, Message message) {
            handleOperation(operation, message);
        }
    };

    protected final ClassMatchedHandler<PutResponse, Message> opResponseMessageClassMatchedHandler = new ClassMatchedHandler<PutResponse, Message>() {
        @Override
        public void handle(PutResponse opResponse, Message message) {
            handleOperationResponse(opResponse, message);
        }
    };

    protected final ClassMatchedHandler<GetResponse, Message> getResponseMessageClassMatchedHandler = new ClassMatchedHandler<GetResponse, Message>() {
        @Override
        public void handle(GetResponse opResponse, Message message) {
            handleOperationResponse(opResponse, message);
        }
    };
    protected final ClassMatchedHandler<CasResponse, Message> casResponseMessageClassMatchedHandler = new ClassMatchedHandler<CasResponse, Message>() {
        @Override
        public void handle(CasResponse opResponse, Message message) {
            handleOperationResponse(opResponse, message);
        }
    };

    private NetAddress getOneNodeFromPartition(Operation msg){

        int key = getPartitionKeyOnMessage(msg);
        Set<NetAddress> p = partitions.get(key);

        if(p.size() > 0){
            return (NetAddress) p.toArray()[1];
        }else{
            return null;
        }
    }


    private NetAddress getOneNodeFromPartitionId(int key){

        Set<NetAddress> p = partitions.get(key);

        if(p.size() > 0){
            return (NetAddress) p.toArray()[1];
        }else{
            return null;
        }
    }


    protected final Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect e) {
            System.out.println("SUSPECTED " + e.p);
            int partitionKey = getPartitionKey(e.p);
            partitions.get(partitionKey).remove(e.p);

            Set<NetAddress> newPartition = partitions.get(partitionKey);

            trigger(new Propose(new StopSignOperation(newPartition, -1), partitionKey), paxos);



            /*

            //Grap a random node in the partition
            NetAddress randomNode = null;
            Set<NetAddress> p = partitions.get(getPartitionKey(e.p));
            for (NetAddress adr : p) {
                if (!e.p.equals(adr)) {
                    randomNode = adr;
                    break;
                }
            }

            if(randomNode == null){
                System.out.println("PARTITION IS DEAD");
            }

            partitions.get(getPartitionKey(e.p)).remove(e.p);

            Set<NetAddress> newPartition = partitions.get(getPartitionKey(e.p));

            trigger(new Message(self, randomNode, new TopologyResponse(newPartition, getPartitionKey(e.p))), net);
            trigger(new Propose(new StopSignOperation(newPartition, 0), getPartitionKey(e.p)), paxos);
            */
        }
    };

    protected final Handler<Restore> restoreHandler = new Handler<Restore>() {
        @Override
        public void handle(Restore e) {
            System.out.println("RESTORED " + e.p);
        }
    };
    protected final Handler<FinalDecide> decideHandler = new Handler<FinalDecide>() {
        @Override
        public void handle(FinalDecide e) {
            System.out.println("DECIDE " + e + " received at " + self);
        }
    };

    protected final Handler<Abort> abortHandler = new Handler<Abort>() {
        @Override
        public void handle(Abort e) {
            System.out.println("ABORT " + e+ " received at " + self);
        }
    };
    /*
    protected final ClassMatchedHandler<TopologyQuery, Message> topologyQueryMessageClassMatchedHandler = new ClassMatchedHandler<TopologyQuery, Message>() {
        @Override
        public void handle(TopologyQuery topologyQuery, Message message) {

            System.out.println("Received topology query from " + message.getSource());
            Set<NetAddress> result = new HashSet<>();
            for (NetAddress adr : active) {
                if(adr.getPort() != self.getPort()){
                    result.add(adr);
                }
            }

            System.out.println(".------");
            System.out.println(result);
            System.out.println(self);
            System.out.println("........");

            trigger(new Message(self, message.getSource(), new TopologyResponse(result, 123)), net);

        }
    };
    */

    {
        //subscribe(topologyQueryMessageClassMatchedHandler, net);
        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(assignmentHandler, boot);
        subscribe(checkinHandler, net);
        subscribe(readyHandler, net);
        subscribe(operationMessageClassMatchedHandler, net);
        subscribe(getOperationMessageClassMatchedHandler, net);
        subscribe(casOperationMessageClassMatchedHandler, net);
        subscribe(opResponseMessageClassMatchedHandler, net);
        subscribe(getResponseMessageClassMatchedHandler, net);
        subscribe(casResponseMessageClassMatchedHandler, net);
        subscribe(suspectHandler, epfd);
        subscribe(restoreHandler, epfd);
        subscribe(decideHandler, paxos);
        subscribe(abortHandler, paxos);
    }

    @Override
    public void tearDown() {
        trigger(new CancelPeriodicTimeout(timeoutId), timer);
    }

    private void bootUp() {
        LOG.info("Threshold reached. Generating assignments...");
        state = State.SEEDING;
        trigger(new GetInitialAssignments(ImmutableSet.copyOf(active)), boot);
    }

    static enum State {

        COLLECTING,
        SEEDING,
        DONE;
    }
}
