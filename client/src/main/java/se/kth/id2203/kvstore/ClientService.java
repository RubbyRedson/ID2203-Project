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
package se.kth.id2203.kvstore;

import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.LoggerFactory;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class ClientService extends ComponentDefinition {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ClientService.class);
    //******* Ports ******
    final Positive<Timer> timer = requires(Timer.class);
    final Positive<Network> net = requires(Network.class);
    //******* Fields ******
    protected final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    protected final NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private final Map<UUID, SettableFuture<OpResponse>> pending = new TreeMap<>();


    protected void doStartupStuff(){
        Operation putKey = new PutOperation("23hash23", "The value");
        trigger(new Message(self, server, putKey), net);
    }

    //******* Handlers ******
    protected final Handler<Start> startHandler = new Handler<Start>() {
        
        @Override
        public void handle(Start event) {
            LOG.debug("Starting client on {}. Waiting to connect...", self);
            long timeout = (config().getValue("id2203.project.keepAlivePeriod", Long.class) * 2);
            ScheduleTimeout st = new ScheduleTimeout(timeout);
            st.setTimeoutEvent(new ConnectTimeout(st));


            doStartupStuff();
            trigger(st, timer);
        }
    };


    private void handleOperationResponse(OpResponse opResponse) {
        LOG.debug("Got OpResponse: {}", opResponse);
//        SettableFuture<OpResponse> sf = pending.remove(opResponse.id);
//        if (sf != null) {
//            sf.set(opResponse);
//        } else {
//            LOG.warn("ID {} was not pending! Ignoring response.", opResponse.id);
//        }

    }


    protected final ClassMatchedHandler<PutResponse, Message> putResponseMessageClassMatchedHandler = new ClassMatchedHandler<PutResponse, Message>() {
        @Override
        public void handle(PutResponse content, Message context) {
            handleOperationResponse(content);
        }
    };

    protected final ClassMatchedHandler<GetResponse, Message> getResponseMessageClassMatchedHandler = new ClassMatchedHandler<GetResponse, Message>() {
        @Override
        public void handle(GetResponse content, Message context) {
            handleOperationResponse(content);
        }
    };

    protected final ClassMatchedHandler<CasResponse, Message> casResponseMessageClassMatchedHandler = new ClassMatchedHandler<CasResponse, Message>() {
        @Override
        public void handle(CasResponse content, Message context) {
            handleOperationResponse(content);
        }
    };

    {
        subscribe(startHandler, control);
        subscribe(putResponseMessageClassMatchedHandler, net);
        subscribe(getResponseMessageClassMatchedHandler, net);
        subscribe(casResponseMessageClassMatchedHandler, net);
    }
    
    Future<OpResponse> op(String key) {
        Operation op = new GetOperation(key);

        //Operation op = new Operation(key);
        OpWithFuture owf = new OpWithFuture(op);
        trigger(owf, onSelf);
        return owf.f;
    }
    
    public static class OpWithFuture implements KompicsEvent {
        
        public final Operation op;
        public final SettableFuture<OpResponse> f;
        
        public OpWithFuture(Operation op) {
            this.op = op;
            this.f = SettableFuture.create();
        }
    }
    
    public static class ConnectTimeout extends Timeout {
        
        ConnectTimeout(ScheduleTimeout st) {
            super(st);
        }
    }
}
