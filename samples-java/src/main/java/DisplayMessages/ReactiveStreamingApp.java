// Copyright (c) Microsoft. All rights reserved.

package DisplayMessages;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

/**
 * Initialize reactive streaming
 */
public class ReactiveStreamingApp {

    private static ActorSystem system = ActorSystem.create("Demo");

    protected final static Materializer streamMaterializer = ActorMaterializer.create(system);
}
