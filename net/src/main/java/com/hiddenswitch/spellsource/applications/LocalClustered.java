package com.hiddenswitch.spellsource.applications;

import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.atomix.core.Atomix;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * The main entry point of a local game server.
 * <p>
 * Starts a local services cluster, tries to migrate and broadcasts over UDP to Unity3D-based clients the IP address of
 * this server once it's ready to be connected to.
 */
public class LocalClustered {
	public static void main(String args[]) {
		Future<String> serverStarted = Future.future();
		startServer(serverStarted);
		serverStarted.setHandler(done -> {
			System.out.println("***** SERVER IS READY. START THE CLIENT. *****");
		});
		// Daemonized
	}

	public static void startServer(Future<String> broadcasterDeployment) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("org.mongodb.async.type", "netty");
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		int atomixPort = Integer.parseInt(System.getenv().getOrDefault("ATOMIX_PORT", "5701"));
		LoggerFactory.initialise();


		// Set significantly longer timeouts
		long nanos = Duration.of(4, ChronoUnit.MINUTES).toNanos();
		Atomix atomix = Cluster.create(atomixPort);
		atomix.start().join();
		ClusterManager clusterManager = new AtomixClusterManager(atomix);
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(clusterManager)
				.setClusterHost(Gateway.getHostAddress())
				.setBlockedThreadCheckInterval(RpcClient.DEFAULT_TIMEOUT)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
//				.setMetricsOptions(Clustered.getMetrics())
				.setInternalBlockingPoolSize(Runtime.getRuntime().availableProcessors() * 40)
				.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors())
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors() * 40), then -> {

			final Vertx vertx = then.result();
			Tracing.initializeGlobal(vertx);

			Spellsource.spellsource().migrate(vertx, v1 -> {
				if (v1.failed()) {
					Logging.root().error("Migration failed", v1.cause());
				} else {
					Spellsource.spellsource().deployAll(vertx, v2 -> {
						if (v2.failed()) {
							Logging.root().error("Deployment failed", v2.cause());
							System.exit(1);
							return;
						}
						vertx.deployVerticle(Broadcaster.create(), broadcasterDeployment);
					});
				}
			});
		});
	}
}
