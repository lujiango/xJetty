package org.github.x.jetty.http.routing;

import java.util.concurrent.LinkedBlockingQueue;
/**
 * ·�ɲ��ԣ�����ϣ��������չ
 * @author lujiango
 *
 * @param <E>
 */
public class CycleQueue<E> extends LinkedBlockingQueue<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8025416110841636830L;

	public synchronized E cycle() {
		E ele = poll();
		if (ele != null) {
			offer(ele);
		}
		return ele;
	}
}