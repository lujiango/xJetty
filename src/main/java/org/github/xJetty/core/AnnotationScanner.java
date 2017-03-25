package org.github.xJetty.core;

import org.apache.log4j.Logger;
import org.github.xJetty.annotation.Entry;
import org.github.xJetty.annotation.Register;
import org.github.xJetty.annotation.ZKConf;

import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.FieldAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.MethodAnnotationObjectDiscoveryListener;

public class AnnotationScanner {
	private static final Logger LOG = Logger.getLogger(AnnotationScanner.class);

	static void scanAnnotations() {
		LOG.info("start scan annotations...");
		long start = System.currentTimeMillis();
		Discoverer discover = new ClasspathDiscoverer();
		discover.addAnnotationListener(new MyClassAnnotationObjectDiscoveryListener());
		discover.addAnnotationListener(new MyFieldAnnotationObjectDiscoveryListener());
		discover.addAnnotationListener(new MyMethodAnnotationObjectDiscoveryListener());
		discover.discover(true, true, true, true, true, true);
		LOG.info("finish scan annotations in " + (System.currentTimeMillis() - start) + "ms");
	}

	static class MyClassAnnotationObjectDiscoveryListener implements
			ClassAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { Entry.class.getName() };
		}

		public void discovered(ClassFile clazz, Annotation annotation) {

		}

	}

	static class MyMethodAnnotationObjectDiscoveryListener implements
			MethodAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { Register.class.getName() };
		}

		public void discovered(ClassFile clazz, MethodInfo method,
				Annotation annotation) {

		}

	}

	static class MyFieldAnnotationObjectDiscoveryListener implements
			FieldAnnotationObjectDiscoveryListener {

		public String[] supportedAnnotations() {
			return new String[] { ZKConf.class.getName() };
		}

		public void discovered(ClassFile clazz, FieldInfo field,
				Annotation annotation) {

		}

	}

}
