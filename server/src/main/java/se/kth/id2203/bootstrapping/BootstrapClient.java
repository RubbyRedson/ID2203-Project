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

import java.awt.print.Book;
import java.util.*;

import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.beb.*;
import se.kth.id2203.broadcast.epfd.EventuallyPerfectFailureDetector;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class BootstrapClient extends ComponentDefinition {

    public static enum State {

        WAITING, STARTED;
    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BootstrapClient.class);
    //******* Ports ******
    final Negative<Bootstrapping> bootstrap = provides(Bootstrapping.class);
    final Positive<Timer> timer = requires(Timer.class);
    final Positive<Network> net = requires(Network.class);
    final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    protected final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);
    //******* Fields ******
    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private final NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private Set<NetAddress> topology = new HashSet<>();

    private State state = State.WAITING;

    private UUID timeoutId;

    //******* Handlers ******
    protected final Handler<Start> startHandler = new Handler<Start>() {

        @Override
        public void handle(Start event) {
            LOG.debug("Starting bootstrap client on {}", self);
            long timeout = (config().getValue("id2203.project.keepAlivePeriod", Long.class) * 2);
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(timeout, timeout);
            spt.setTimeoutEvent(new BSTimeout(spt));
            trigger(spt, timer);
            timeoutId = spt.getTimeoutEvent().getTimeoutId();
        }
    };
    protected final Handler<BSTimeout> timeoutHandler = new Handler<BSTimeout>() {

        @Override
        public void handle(BSTimeout e) {

            if (state == State.WAITING) {
                trigger(new Message(self, server, CheckIn.event), net);
            } else if (state == State.STARTED) {
                trigger(new Message(self, server, Ready.event), net);
                suicide();
            }
        }
    };

    protected final ClassMatchedHandler<Boot, Message> bootHandler = new ClassMatchedHandler<Boot, Message>() {

        @Override
        public void handle(Boot content, Message context) {
            if (state == State.WAITING) {
                LOG.info("{} Booting up.", self);
                trigger(new Booted(content.assignment), bootstrap);
                trigger(new CancelPeriodicTimeout(timeoutId), timer);
                trigger(new Message(self, server, Ready.event), net);
                state = State.STARTED;
            }
        }
    };


    protected final Handler<BEB_Deliver> beb_deliverHandler = new Handler<BEB_Deliver>() {
        @Override
        public void handle(BEB_Deliver beb_deliver) {

//            trigger(new BEB_Broadcast(beb_deliver.payload, topology), beb);
            System.out.println("Saved " + beb_deliver.payload + " at " + self);
        }
    };


    protected final ClassMatchedHandler<TopologyResponse, Message> topologyResponseMessageClassMatchedHandler = new ClassMatchedHandler<TopologyResponse, Message>() {
        @Override
        public void handle(TopologyResponse topologyResponse, Message message) {
            topology = topologyResponse.topology;

            System.out.println("----- Topisie ---");
            topology.remove(server);
            System.out.println(topology);
            System.out.println("-----------------");
        }
    };
    protected final ClassMatchedHandler<PutKey, Message> putKeyHandler = new ClassMatchedHandler<PutKey, Message>() {
        @Override
        public void handle(PutKey putKey, Message message) {
            System.out.println("put key at " + self.getPort());
            trigger(new BEB_Broadcast(putKey, topology), beb);
        }
    };

    @Override
    public void tearDown() {
        trigger(new CancelPeriodicTimeout(timeoutId), timer);
    }

    {
        subscribe(putKeyHandler, net);
        subscribe(beb_deliverHandler, beb);
        subscribe(topologyResponseMessageClassMatchedHandler, net);
        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(bootHandler, net);
    }
}
