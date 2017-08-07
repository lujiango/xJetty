package org.github.x.jetty.core;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class Startup {
	private static final Logger LOG = Logger.getLogger(Startup.class);

	public static void startup() {
		LOG.info("Startup method...");
		TreeSet<EntryInfo> ts = new TreeSet<EntryInfo>(new Comparator<EntryInfo>() {
			public int compare(EntryInfo o1, EntryInfo o2) {
				int ret = o1.startup - o2.startup;

				if (ret == 0) {
					ret = o1.hashCode() - o2.hashCode();
				}
				return ret;
			}

			@Override
			public boolean equals(Object obj) {
				return this == obj;
			}

			@Override
			public int hashCode() {
				return super.hashCode();
			}
		});
		for (EntryInfo ai : AnnotationScanner.url2method.values()) {
			if (ai.startup > Integer.MIN_VALUE) {
				ts.add(ai);
			}
		}
		for (EntryInfo ai : ts) {
			try {
				String methodDesc = ai.clazz.getName() + '/' + ai.method.getName();
				LOG.info("Startup method #" + ai.startup + ' ' + methodDesc + " processing...");
				if (ai.paramTypes.length != 0) {
					LOG.error("Startup method #" + ai.startup + " error, should not has any parameters.");
				} else {
					long start = System.currentTimeMillis();
					if (Modifier.isStatic(ai.method.getModifiers())) {
						ai.method.invoke(null, new Object[0]);
					} else {
						Object service = ai.clazz.newInstance();
						ai.method.invoke(service, new Object[0]);
					}
					LOG.info("Startup method #" + ai.startup + " finished in " + (System.currentTimeMillis() - start) + "ms.");
				}
			} catch (Exception e) {
				LOG.info("Startup method #" + ai.startup + " execute failed. ", e);
			}

		}
	}
}
