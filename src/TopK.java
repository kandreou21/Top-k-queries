//Konstantinos Andreou 4316
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class TopK {
	int k = 15;
	
	public float[] readRndFile() {
		BufferedReader br;
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		try {
			br = new BufferedReader(new FileReader("rnd.txt"));	
			while ((line = br.readLine()) != null) {
			    lines.add(line);		
			}
		} catch (IOException e) {}		
		float[] R = new float[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			String[] lineValues = lines.get(i).split(" ");
			R[Integer.parseInt(lineValues[0])] = Float.parseFloat(lineValues[1]);
		}
		return R;
	}

	public PriorityQueue<Record> readSeqFiles() {
		float[] R = readRndFile();
		HashMap<Integer, Record> recordsFound = new HashMap<Integer, Record>();
		PriorityQueue<Record> minHeap = null;
		float threshold = 0;
		int accesses = 0;
		BufferedReader seq1, seq2;
		String line1, line2 = "";
		try {
			seq1 = new BufferedReader(new FileReader("seq1.txt"));	
			seq2 = new BufferedReader(new FileReader("seq2.txt"));
			float value1, value2 = 0;
			while ((line1 = seq1.readLine()) != null && (line2 = seq2.readLine()) != null) {
				String[] lineValues = line1.split(" ");
				int id = Integer.parseInt(lineValues[0]);
				value1 = Float.parseFloat(lineValues[1]);
				
				if (!recordsFound.containsKey(id)) {
					Record record = new Record(id, R[id]+value1, 1);
					recordsFound.put(id, record);	
				} else {
					recordsFound.get(id).setTotalScore(value1);		
				}
				accesses++;
				threshold = value1 + value2 + 5f;
				
				if (minHeap == null) {
					minHeap = initialiseMinHeap(recordsFound, minHeap);	
				} else {
					checkPush(recordsFound, minHeap, id);	
					if (minHeap.peek().getCurrentScore() >= threshold) { //checking for termination
						boolean stopFlag = checkTermination(recordsFound, minHeap, value1, value2);
						if (stopFlag == true) {
							System.out.println("Number of sequential accesses= " + accesses);
							System.out.println("line1: " + line1 + "line2" + line2);
							return minHeap;
						}
					}
				}
				
				//seq2
				lineValues = line2.split(" ");
				id = Integer.parseInt(lineValues[0]);
				value2 = Float.parseFloat(lineValues[1]);
				
				if (!recordsFound.containsKey(id)) {
					Record record = new Record(id, R[id]+value2, 2);
					recordsFound.put(id, record);
				} else {
					recordsFound.get(id).setTotalScore(value2);
				}
				accesses++;
				threshold = value1 + value2 + 5f;	
				
				if (minHeap == null) {
					minHeap = initialiseMinHeap(recordsFound, minHeap);	
				} else {
					checkPush(recordsFound, minHeap, id);	
					if (minHeap.peek().getCurrentScore() >= threshold) { //checking for termination
						boolean stopFlag = checkTermination(recordsFound, minHeap, value1, value2);
						if (stopFlag == true) {
							System.out.println("Number of sequential accesses= " + accesses);
							System.out.println("line1: " + line1 + "line2" + line2);
							return minHeap;
						}
					}
				}
			}			
		} catch (IOException e) {}		
		System.out.println("Number of sequential accesses= " + accesses);		
		return minHeap;
	}

	private boolean checkTermination(HashMap<Integer, Record> recordsFound, PriorityQueue<Record> minHeap,
			float value1, float value2) {
		for (Record record : recordsFound.values()) {
			if (!minHeap.contains(record)) {
				float upperBound;
				if (record.fileShown == 1) {
					upperBound = record.getLowerBound() + value2;  
				} else {
					upperBound = record.getLowerBound() + value1;  
				}
				if (upperBound > minHeap.peek().getCurrentScore()) {
					return false;
				}	
			} 
		}
		return true;
	}

	private void checkPush(HashMap<Integer, Record> recordsFound, PriorityQueue<Record> minHeap, int id) {
		if (!minHeap.contains(recordsFound.get(id))) {
			if (minHeap.peek().getCurrentScore() < recordsFound.get(id).getCurrentScore()) {
				System.out.println("joined" + id);
				minHeap.poll();
				minHeap.add(recordsFound.get(id));
			}
		}
	}

	private PriorityQueue<Record> initialiseMinHeap(HashMap<Integer, Record> recordsFound, PriorityQueue<Record> minHeap) {
		if (recordsFound.size() == k) {
			minHeap = new PriorityQueue<Record>(recordsFound.values());
		}
		return minHeap;
	}
	
	public FloatWithIndex[] checkBruteForce(){ 
		float[] R = readRndFile();
		BufferedReader seq1;
		BufferedReader seq2;
		String line1, line2;
		try {
			seq1 = new BufferedReader(new FileReader("seq1.txt"));
			seq2 = new BufferedReader(new FileReader("seq2.txt"));	
			while ((line1 = seq1.readLine()) != null && (line2 = seq2.readLine()) != null) {
				String[] lineValues = line1.split(" ");
				sum(R, lineValues); 	
				lineValues = line2.split(" ");
				sum(R, lineValues);
			}
		} catch (IOException e) {}
		FloatWithIndex[] arrWithIndex = new FloatWithIndex[R.length];
        for (int i = 0; i < R.length; i++) {
            arrWithIndex[i] = new FloatWithIndex(R[i], i);
        }
        Arrays.sort(arrWithIndex, new Comparator<FloatWithIndex>() {
            @Override
            public int compare(FloatWithIndex f1, FloatWithIndex f2) {
                return Float.compare(f2.value, f1.value);
            }
        });
        return arrWithIndex;
	}

	private void sum(float[] R, String[] lineValues) { 
		int index = Integer.parseInt(lineValues[0]);
		float value = Float.parseFloat(lineValues[1]);
		R[index] += value;
	}
	
	private class FloatWithIndex {
	    float value;
	    int index;

	    public FloatWithIndex(float value, int index) {
	        this.value = value;
	        this.index = index;
	    }
	}
	
	public void printTopKs(PriorityQueue<Record> minHeap) {
		FloatWithIndex[] bruteForce = checkBruteForce();
		ArrayList<Record> tops = new ArrayList<Record>();
		int counter = 0;
		for (int i = 0; i < k; i++) {
			Record record = minHeap.poll();
			tops.add(record);
		}
		Collections.reverse(tops);
		System.out.println("Top k objects:");	
		for (int i = 0; i < k; i++) {
			System.out.println(counter + " " + tops.get(i).getId() + ": " + String.format("%.2f", tops.get(i).getCurrentScore()) + "	" + bruteForce[i].index + ": " + String.format("%.2f", bruteForce[i].value));
			counter++;
		}
	}
	
	public static void main(String[] args) {
		TopK topK = new TopK();
		//topK.k = Integer.parseInt(args[0]);
		//topK.checkBruteForce();
		PriorityQueue<Record> minHeap = topK.readSeqFiles();
		topK.printTopKs(minHeap);
	}	
}