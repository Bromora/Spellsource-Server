package com.hiddenswitch.spellsource;

import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * This class makes it easy to make a directory full of JSON files available to the game to use as cards. Implementors
 * of this class will make the JSON files located in the the directory {@code thisModulePath + "/src/main/resources/" +
 * getDirectoryPrefix()} available to the {@code CardCatalogue}.
 * <h2>What does this do?</h2>
 * At a high level, Spellsource can support many ways of loading cards into the game. However, in practice, the only way
 * cards are specified is as JSON files located in various modules, or specially-authored directories containing Java
 * code.
 * <p>
 * Extending this class allows you to override the {@link #getDirectoryPrefix()} method to quickly create a "cards
 * plugin" that the core game code can use to find cards.
 * <h2>How exactly should I use this class?</h2>
 * Depends how much Java stuff you know.
 * <p>
 * It's complicated, because Java uses a mixture of convention, configuration and magic to achieve really basic things,
 * like helping a piece of code find files.
 * <p>
 * If you are editing the {@code Spellsource-Server} project inside IntelliJ, you'll observe that you have directories
 * in your Project View with little blue rectangles over them. These are Java "modules," which are like "dll"
 * files--pieces of computer code and files that are packaged up in a special way. We use modules to make it easy to
 * distribute small pieces of code and small amounts of files, which is a perfect fit for Spellsource cards.
 * <p>
 * A module is basically a contract to put code and text files in special places to help a system figure out how to
 * build the {@code Spellsource} game. This class is part of the contract to help find cards. So to add cards in the
 * game in a way that is totally separate from the {@code Spellsource-Server} code, you'll need to obey these same
 * contracts.
 * <p>
 * There are two pieces to this contract. The first is a class that extends this one. Create it in the {@code
 * com.hiddenswitch.spellsource} package, which will be in a folder like {@code src/main/java/com/hiddenswitch/spellsource}.
 * <p>
 * Then, create a class that extends {@link CardsModule} in the same package.
 * <p>
 * Finally, put cards in the directory you specify by overriding {@link #getDirectoryPrefix()}.
 * <h2>Where exactly to cards go?</h2>
 * Java typically packages things like JSON files as "resources" inside a "jar," or Java Archive. Jars are like the
 * "exe" or "dll" files of the Java world, containing a mix of data and code that applications can use.
 * <p>
 * You'll observe that if you view inheritors of this class, they override the {@link #getDirectoryPrefix()} method. The
 * class is typically located in a folder that exists on a path that reads, in part, {@code
 * src/main/java/com/hiddenswitch/spellsource}. The folder that contains the cards will be located in {@code
 * src/main/resources/}, followed by the string returned by {@link #getDirectoryPrefix()}. For example, if the string
 * returned is {@code "testcards"}, the path where the card JSON files go is {@code src/main/resources/testcards}.
 * <h2>How do the cards get loaded?</h2>
 * Spellsource uses Google Guice to find all the {@link CardsModule} classes on the classpath, loads their {@link
 * CardResources}, and adds all those cards to the {@code CardCatalogue}.
 * <p>
 * The classpath is like a list of files (Java Archives that we talked about earlier) that are made "available" to
 * Spellsource. Only code and files specified on the classpath is visible to Spellsource.
 * <h2>What is the relationship between this card loading process and the game?</h2>
 * You probably mean the client, what you downloaded through the launcher or accessed via <a
 * href="https://playspellsource.com">playspellsource.com</a>. There is no relationship between the client and the
 * process described above. The client cannot and does not load cards.
 *
 * @param <T> The implementing class.
 */
public abstract class AbstractCardResources<T> implements CardResources {
	protected final Logger LOGGER;
	private final Class<T> thisClass;
	private AtomicBoolean isLoaded = new AtomicBoolean();
	private List<ResourceInputStream> resources;

	protected AbstractCardResources(Class<T> thisClass) {
		this.thisClass = thisClass;
		LOGGER = LoggerFactory.getLogger(thisClass);
	}

	@Override
	public void load() {
		if (!isLoaded.compareAndSet(false, true)) {
			return;
		}
		try {
			ClassLoader classLoader = thisClass.getClassLoader();
			resources = ClassPath.from(classLoader)
					.getResources()
					.stream()
					.filter(resource -> resource.getResourceName().startsWith(getDirectoryPrefix()) && resource.getResourceName().endsWith(".json"))
					.map(resource -> new ResourceInputStream(resource.getResourceName(), CardResources.getInputStream(classLoader, true, resource.getResourceName()), false))
					.collect(Collectors.toList());
			LOGGER.debug("load {}: {} cards", getDirectoryPrefix(), resources.size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * The location in the {@code resources} directory where card JSON files are located.
	 *
	 * @return A string.
	 */
	abstract String getDirectoryPrefix();

	@Override
	public List<ResourceInputStream> getResources() {
		load();
		return resources;
	}

}
