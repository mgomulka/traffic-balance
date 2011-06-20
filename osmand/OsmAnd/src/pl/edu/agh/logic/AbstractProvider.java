package pl.edu.agh.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractProvider<L> {
	
	protected interface ListenerTask<L> {
		public void execute(L listener);
	}

	protected List<L> listeners = Collections.synchronizedList(new ArrayList<L>());
	
	public void registerListener(L listener) {
		listeners.add(listener);
	}
	
	public void unregisterListener(L listener) {
		listeners.remove(listener);
	}
	
	protected void unregisterAll() {
		listeners.clear();
	}
	
	protected void forAllListeners(ListenerTask<L> task) {
		for (L listener : listeners) {
			task.execute(listener);
		}
	}
}
