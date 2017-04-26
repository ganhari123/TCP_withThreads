import java.net.*;
import java.util.*;
import java.io.*;

public class Window <T> {
	private ArrayDeque<T> window;
	private LinkedList<T> sendFileWindow;
	private int capac;
	private int currAmount;

	public Window(int capac) {
		this.capac = capac;
		window = new ArrayDeque(capac);
		currAmount = 0;
		sendFileWindow = new LinkedList();
	}

	public LinkedList<T> getSendFileWindow() {
		synchronized (sendFileWindow) {
			return sendFileWindow;
		}
		
	}

	public synchronized void removeElement(int index) {
		while (index >= currAmount) {
			try {
				System.out.println("Waiting to remove element");
				wait();
			} catch (InterruptedException e) {
				System.out.println("Inter exception");
			}
		}
		sendFileWindow.remove(index);
		currAmount--;
		notify();


	}

	public synchronized void addElement(T element) {
		while (currAmount >= capac) {
			try {
				System.out.println("Waiting to add element");
				wait();
			} catch (InterruptedException e) {
				System.out.println("Inter exception");
			}
		}

		sendFileWindow.addLast(element);
		currAmount++;
		notify();
	}

	public synchronized T popHead() {
		while (currAmount == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Inter exception");
			}
		}

		T returnFrame = window.remove();
		currAmount--;
		notify();
		return returnFrame;

	}

	public void resetWindow() {
		window = new ArrayDeque<T>(capac);
		currAmount = 0;
	}

	public synchronized void addPacket(T packet) {
		while (currAmount >= capac) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Inter exception");
			}

		}
		window.add(packet);
		currAmount++;
		notify();
	}

	public ArrayDeque getWindow() {
		return window;
	}

	public void setWindow(ArrayDeque<T> val) {
		this.window = val;
	}

	public int getCapac() {
		return capac;
	}

	public void setCapac(int val) {
		capac = val;
	}

	public int getCurrAmount() {
		return currAmount;
	}

	public void setCurrAmount(int val) {
		currAmount = val;
	}
}
