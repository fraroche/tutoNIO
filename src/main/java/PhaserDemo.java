import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReferenceArray;

/*
 * Author: Ashwin Jayaprakash
 */
public class PhaserDemo {
	public static void main(String[] args) {
		final int workers = 2;
		final int workLength = 10;

		// le +1 correspont au thread "Main", qui doit avoir fini de lancer tous les autres 
		// threads participant au phaser lors de la phase d'initialisation (phase 0) avant de se 
		// dé-enregistrer.
		// Concretement on a 3 participants à la phase 0 (phase d'init) qui sont les 2 threads 
		// producteurs + le thread "Main",
		// et seulement 2 participants pour les phases suivantes (les 2 threads producteurs).
		final Phaser phaser = new Phaser(workers + 1);
		final AtomicReferenceArray<String> lane1 = new AtomicReferenceArray<String>(new String[workLength]);
		final AtomicReferenceArray<String> lane2 = new AtomicReferenceArray<String>(new String[workLength]);

		new Thread("Producer 1") {
			@Override
			public void run() {
				for (int i = 0; i < workLength; i++) {
					$sleep(20);

					lane1.set(i, "lane1-answer-" + i);

					System.out.printf("[%-17s] working in lane1 finished phase [%d]%n", Thread.currentThread().getName(), phaser.getPhase());

					phaser.arriveAndAwaitAdvance();
				}
			}
		}.start();

		new Thread("Slower producer 2") {
			@Override
			public void run() {
				for (int i = 0; i < workLength; i++) {
					$sleep(40);

					lane2.set(i, "lane2-answer-" + i);

					System.out.printf("[%-17s] working in lane2 finished phase [%d]%n", Thread.currentThread().getName(), phaser.getPhase());

					phaser.arriveAndAwaitAdvance();
				}
			}
		}.start();

		new Thread("Slow consumer") {
			@Override
			public void run() {
				for (int start = 0; start < workLength;) {
					System.out.printf("[%-17s] about to wait for phase [%d] completion%n", Thread.currentThread().getName(), start);

					int phaseInProgress = phaser.awaitAdvance(start);

					// Read all the way up to the most recent completed phases.
					for (int i = start; i < phaseInProgress; i++) {
						System.out.printf("[%-17s] read [%s] & [%s] from phase [%d]%n", Thread.currentThread().getName(), lane1.get(i), lane2.get(i), i);
					}

					start = phaseInProgress;

					$sleep(80);
				}
			}
		}.start();

		// finalise la phase 0 d'initialisation en signalant au phaser l'arrivée
		// du thread "Main" et en retirant sa participation aux phases ultérieures.
		phaser.arriveAndDeregister();
	}

	private static void $sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}