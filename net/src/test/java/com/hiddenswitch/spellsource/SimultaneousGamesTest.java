package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class SimultaneousGamesTest extends SpellsourceTestBase {
	private static Logger logger = LoggerFactory.getLogger(SimultaneousGamesTest.class);

	@Test(timeout = 80000L)
	public void testSimultaneousGames(TestContext context) throws InterruptedException, SuspendExecution {
		Tracer tracer = Tracing.initialize("test");
		Span span = tracer.buildSpan("testSimultaneousGames").start();
		int count = Math.max((Runtime.getRuntime().availableProcessors() / 2 - 1) * 2, 2) * 3;
		int checkpointTotal = count * 6;

		Vertx vertx = contextRule.vertx();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor("simultaneousGames", count, 80000L, TimeUnit.MILLISECONDS);
		AtomicInteger checkpoints = new AtomicInteger(0);
		for (int i = 0; i < count; i++) {
			executor.executeBlocking((fut) -> {
				try (UnityClient client = new UnityClient(context) {
					@Override
					protected int getActionIndex(ServerToClientMessage message) {
						// Always return end turn so that we end the game in a fatigue duel
						if (message.getActions().getEndTurn() != null) {
							return message.getActions().getEndTurn();
						} else {
							return super.getActionIndex(message);
						}
					}
				}) {
					client.createUserAccount(null);
					String userId = client.getAccount().getId();
					logger.trace("testSimultaneousGames: {} 1st Matchmaking on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.matchmakeConstructedPlay(null);
					logger.trace("testSimultaneousGames: {} 1st Starts on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.waitUntilDone();
					context.assertTrue(client.getTurnsPlayed() > 0);
					context.assertTrue(client.isGameOver());
//					context.assertFalse(client.getApi().getAccount(userId).getAccounts().get(0).isInMatch());
					logger.trace("testSimultaneousGames: {} 1st Finished {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);

					// Try two games in a row
					logger.trace("testSimultaneousGames: {} 2nd Matchmaking on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.matchmakeConstructedPlay(null);
					logger.trace("testSimultaneousGames: {} 2nd Starts on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.waitUntilDone();
					context.assertTrue(client.getTurnsPlayed() > 0);
					context.assertTrue(client.isGameOver());
//					context.assertFalse(client.getApi().getAccount(userId).getAccounts().get(0).isInMatch());
					logger.trace("testSimultaneousGames: {} 2nd Finished {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					logger.info("testSimultaneousGames: {} finished", userId);

					fut.complete();
				} catch (Throwable any) {
					Tracing.error(any, span, true);
					fut.fail(any);
				}
			}, false, context.asyncAssertSuccess());
		}
	}
}
