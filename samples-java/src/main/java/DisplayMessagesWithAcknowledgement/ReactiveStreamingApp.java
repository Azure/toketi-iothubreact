// Copyright (c) Microsoft. All rights reserved.

package DisplayMessagesWithAcknowledgement;

import akka.actor.ActorSystem;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.Materializer;
import akka.stream.Supervision;

import static java.lang.System.out;

/**
 * Initialize reactive streaming
 */
public class ReactiveStreamingApp
{
    private static final ActorSystem system = ActorSystem.create("Demo");

    private static final Function<Throwable, Supervision.Directive> decider = exc -> {
        out.println("Error: " + exc.getClass().getName() + ": " + exc.getMessage());
        exc.printStackTrace();

        return Supervision.stop();
    };

    protected static final Materializer streamMaterializer =
            ActorMaterializer.create(
                    ActorMaterializerSettings.create(system).withSupervisionStrategy(decider),
                    system);
}
