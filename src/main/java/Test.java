import java.util.concurrent.Phaser;


public class Test {
	public static void main(String[] args) {
		final Phaser phaser = new Phaser();

		for (int i = 0; i < 5; i++) {
			// On enregistre un nouveau thread dans le phaser :
			phaser.register();

			new Thread() {
				@Override
				public void run() {
					System.out.println("Étape 1 : " + this.getName());

					phaser.arriveAndAwaitAdvance();

					System.out.println("Étape 2 : " + this.getName());

					phaser.arriveAndAwaitAdvance();

					System.out.println("Étape 3 : " + this.getName());

					phaser.arriveAndDeregister();
				}
			}.start();
		}
	}
}
