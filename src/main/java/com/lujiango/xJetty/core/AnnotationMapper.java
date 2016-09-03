package com.lujiango.xJetty.core;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import com.impetus.annovention.listener.MethodAnnotationDiscoveryListener;
import com.impetus.annovention.listener.MethodAnnotationObjectDiscoveryListener;
import com.lujiango.xJetty.annotation.Entry;

public class AnnotationMapper {
	
	static class MyClassAnnotationObjectDiscoveryListener implements ClassAnnotationObjectDiscoveryListener{

		public String[] supportedAnnotations() {
			return new String[]{Entry.class.getName()};
		}

		public void discovered(ClassFile arg0, Annotation arg1) {
			
		}
		
	}
	
	static class MyMethodAnnotationObjectDiscoveryListener implements MethodAnnotationObjectDiscoveryListener{

		@Override
		public String[] supportedAnnotations() {
			return new String[]{};
		}

		@Override
		public void discovered(ClassFile arg0, MethodInfo arg1, Annotation arg2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	

}
