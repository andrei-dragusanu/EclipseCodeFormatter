package krasa.formatter.adapter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import org.junit.Assert;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;


public class UpdateLibs {

	private static final File CLASSLOADERS = new File("./src/java/krasa/formatter/eclipse/Classloaders.java");

	public static void main(String[] args) throws IOException {
		Assert.assertTrue(CLASSLOADERS.exists());
		new UpdateLibs().run();
	}

	private void run() throws IOException {
		String to = "lib/eclipse45";
		String from = "F:/workspace/eclipse-cpp-mars-1-win32";

		Map<String, String> oldJars = getJarsToUpdate(to);
		for (Map.Entry<String, String> jarName : oldJars.entrySet()) {
			System.out.println(jarName);
		}

		List<File> jarsToCopy = getJarsToCopy(from, oldJars.keySet());

		copyJars(to, jarsToCopy);
		
		updateClassloaders(jarsToCopy, oldJars);

	}

	private void updateClassloaders(List<File> jarsToCopy, Map<String, String> oldJars) throws IOException {
		String s = readFileToString(CLASSLOADERS);

		for (File file : jarsToCopy) {
			String prefix = jarPrefix(file.getName());
			String oldJar = oldJars.get(prefix);
			s = s.replace(oldJar, file.getName());
		}
		write(CLASSLOADERS, s);
	}

	@NotNull
	private String jarPrefix(String name) {
		int i = name.indexOf("_");
		return name.substring(0, i);
	}

	private void copyJars(String to, List<File> jarsToCopy) {
		for (File file : jarsToCopy) {
			File to1 = new File(to, file.getName());
			try {
				System.out.println("copying " + file.getName());
				Files.copy(file, to1);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	@NotNull
	private List<File> getJarsToCopy(String from, Set<String> jarNames) {
		List<File> jarsToCopy = new ArrayList<File>();
		Iterator<File> eclipseJars = Files.fileTreeTraverser().breadthFirstTraversal(new File(from)).filter(
				new Predicate<File>() {
					@Override
					public boolean apply(File file) {
						return file.getName().endsWith(".jar");
					}
				}).iterator();
		while (eclipseJars.hasNext()) {
			File next = eclipseJars.next();
			String name = next.getName();
			int i = name.indexOf("_");
			if (i <= 0)
				continue;
			if (jarNames.contains(name.substring(0, i))) {
				jarsToCopy.add(next);
			}
		}
		return jarsToCopy;
	}

	@NotNull
	private Map<String, String> getJarsToUpdate(String to) {
		Map<String, String> oldJars = new HashMap<String, String>();
		Iterator<File> iterator = Files.fileTreeTraverser().children(new File(to)).iterator();
		while (iterator.hasNext()) {
			File next = iterator.next();
			String name = next.getName();
			if (name.endsWith(".jar")) {
				int i = name.indexOf("_");
				if (i <= 0)
					continue;
				oldJars.put(name.substring(0, i), name);
			}
		}
		return oldJars;
	}

}
