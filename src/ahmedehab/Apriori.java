package ahmedehab;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.*;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
public class Apriori { 

    public class Item implements Comparable<Item> {
	List<String> itemValue = new ArrayList<>();
	int support; 

	public Item(List<String> itemValue, int support) {
	    super();
	    this.itemValue = itemValue;
	    this.support = support;
	}

	@Override
	public int compareTo(Item arg0) {
	    if (this.support > arg0.support) {
		return -1;
	    } else {
		return 1;
	    }
	}

    }

    int longestTransactionSize = 0;
    List<Item> frequentItems = new ArrayList<>();
    List<List<String>> transactions = new ArrayList<>();
    List<List<String>> frequentSet = new ArrayList<>();
    List<List<String>> candidateSet = new ArrayList<>();
    HashMap<List<String>, Integer> candidateSetSupport = new HashMap<>();

    int dbsize = 0;
    int minsup = 0;

    public static void main(String[] args) throws IOException {
	Apriori ap = new Apriori();
	ap.aprioriAlgo();
    }

    public void aprioriAlgo() {

	try {

	    FileDialog dialog = new FileDialog((Frame) null, "Select File to Open");
	    dialog.setMode(FileDialog.LOAD);
	    dialog.setVisible(true);
	    String file = dialog.getDirectory() + "\\" + dialog.getFile();
	    String delimiter = JOptionPane.showInputDialog(null,
		    "Write your delimiter to specify boundaries of your data");
	    this.readyTransactions(file, delimiter);
	} catch (IOException e) {
	    System.out.println("Error in opening the file to ready transactions");
	    e.printStackTrace();
	}

	this.minsup = (int) (this.dbsize
		* Double.parseDouble(JOptionPane.showInputDialog("Enter minsup in decimal(less than 1) like 0.6")));
	// This loop is for first level only
	for (List<String> stringArray : this.transactions) {
	    for (String singleItem : stringArray) {
		List<String> temp = new ArrayList<String>();
		temp.add(singleItem);
		this.addSupport(temp);
	    }
	}
	this.addFrequents();

	this.candidateSet = this.generateCandidates();
	this.calculateSupport();
	for (List<String> candidate : this.candidateSet) {
	    System.out.println(candidate);
	}

	this.addFrequents();
	// this.frequentSet = this.candidateSet;
	this.candidateSet = this.generateCandidates();
	this.calculateSupport();
	for (List<String> candidate : this.candidateSet) {
	    System.out.println(candidate);
	}
	while (this.candidateSet.size() > 1) {
	    // if( this.candidateSet.get(0).size() >
	    // this.longestTransactionSize){
	    this.addFrequents();
	    // this.frequentSet = this.candidateSet;
	    this.candidateSet = this.generateCandidates();
	    this.calculateSupport();
	    // this.addFrequents();
	    for (List<String> candidate : this.candidateSet) {
		System.out.println(candidate);
	    }
	    // }
	}
	JFileChooser chooser = new JFileChooser();
	chooser.setCurrentDirectory(new java.io.File("."));
	chooser.setDialogTitle("Choose where to save your file");
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	chooser.setAcceptAllFileFilterUsed(false);
	chooser.showOpenDialog(null);
	try {
	    PrintWriter out = new PrintWriter(chooser.getSelectedFile()+"\\AprioriOutput.txt");

	    this.sortItemList(this.frequentItems);
	    for (Item item : this.frequentItems) {
		out.println(item.support + ":" + item.itemValue);
		System.out.println(item.support + ":" + item.itemValue);
	    }
	    out.close();
	} catch (FileNotFoundException e) {
	}

    }

    public void readyTransactions(String path, String delimiter) throws IOException {
	File file = new File(path);
	FileReader fileReader = new FileReader(file);
	BufferedReader bufferedReader = new BufferedReader(fileReader);
	StringBuffer stringBuffer = new StringBuffer();
	String line;
	while ((line = bufferedReader.readLine()) != null) {
	    this.transactions.add(Arrays.asList(line.split(delimiter)));
	    this.longestTransactionSize = Math.max(this.longestTransactionSize,
		    Arrays.asList(line.split(delimiter)).size());
	    this.dbsize++;
	    stringBuffer.append(line);
	    stringBuffer.append("\n");
	}

	fileReader.close();
    }

    public void sortItemList(List<Item> list) {
	Collections.sort(list, new Comparator<Item>() {
	    @Override
	    public int compare(Item a, Item b) {
		return a.compareTo(b);
	    }
	});
    }

    public void calculateSupport() {
	for (List<String> transaction : this.transactions) {
	    for (List<String> candidate : candidateSet) {
		boolean exist = true;
		for (String item : candidate) {
		    if (transaction.contains(item) && exist) {
			exist = true;
		    } else {
			exist = false;
		    }
		}
		if (exist) {
		    this.addSupport(candidate);
		}
	    }
	}
    }

    public void addSupport(List<String> item) {
	if (this.candidateSet.contains(item)) {
	    if (candidateSetSupport.containsKey(item)) {
		int support = candidateSetSupport.get(item).intValue();
		support++;
		candidateSetSupport.put(item, support);
	    } else {
		candidateSetSupport.put(item, 1);
	    }
	} else {
	    candidateSet.add(item);
	    candidateSetSupport.put(item, 1);
	}
    }

    public void addFrequents() {
	for (List<String> candidate : candidateSet) {
	    if (candidateSetSupport.containsKey(candidate)) {
		if (candidateSetSupport.get(candidate) >= this.minsup) {
		    this.frequentSet.add(candidate);
		    this.frequentItems.add(new Item(candidate, this.candidateSetSupport.get(candidate)));
		}
	    }
	}
	// this.resetFrequents();
	this.candidateSet = new ArrayList<>();
	this.candidateSetSupport = new HashMap<>();
    }

    public int getSupport(String item) {
	if (!this.candidateSetSupport.containsKey(item)) {
	    return 0;
	} else {
	    return this.candidateSetSupport.get(item);
	}
    }

    public List<List<String>> generateCandidates() {
	List<List<String>> candidates = new ArrayList<>();
	int resultPosition = 0;
	int i = 1;
	while (resultPosition < this.frequentSet.size()) {

	    List<String> tempCandidate = new ArrayList<>();
	    if (resultPosition + i >= this.frequentSet.size()) {
		resultPosition++;
		i = 1;

		if (resultPosition >= this.frequentSet.size() || resultPosition + i >= this.frequentSet.size()) {

		    this.sortListOfList(candidates);
		    this.resetFrequents();
		    return candidates;
		}
	    }
	    tempCandidate = this.joinItems(frequentSet.get(resultPosition), frequentSet.get(i + resultPosition));
	    if (tempCandidate != null && tempCandidate.size() > 0) {
		candidates.add(tempCandidate);
	    } else {
		resultPosition++;
		i = 0;
	    }
	    i++;
	}

	this.sortListOfList(candidates);
	this.resetFrequents();
	return candidates;
    }

    public void sortListOfList(List<List<String>> listOfList) {
	Collections.sort(listOfList, new Comparator<List<String>>() {
	    @Override
	    public int compare(List<String> a, List<String> b) {
		a.sort(null);
		b.sort(null);
		int i = 0;
		while (i < a.size()) {
		    if (!a.get(i).equals(b.get(i))) {
			return a.get(i).compareTo(b.get(i));
		    }
		    i++;
		}
		return a.get(0).compareTo(b.get(0));
	    }
	});
    }

    public List<String> joinItems(List<String> l1, List<String> l2) {
	if (l1.equals(l2)) {
	    return null;
	}
	if (l1.size() != l2.size()) {
	    return null;
	}
	List<String> result = new ArrayList<>();
	boolean equal = true;
	if (l1.size() == 1 && l2.size() == 1) {
	    result.add(l1.get(0));
	    result.add(l2.get(0));
	} else {
	    for (int i = 0; i < l1.size() - 1; i++) {
		if (l1.get(i).equals(l2.get(i)) && equal) {
		    equal = true;
		} else {
		    equal = false;
		}
	    }
	    if (equal) {
		result.addAll(l1.subList(0, (l2.size() - 1)));
		result.add(l1.get(l1.size() - 1));
		result.add(l2.get(l2.size() - 1));
	    }
	}
	result.sort(null);
	return result;

    }

    public void resetFrequents() {
	this.frequentSet = new ArrayList<>();
    }

}
