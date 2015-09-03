

/*
 * Vaishakhi Kulkarni
 * Net Id:vpk14030
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DecisionTreePostPruningFinal  
{

	// ENUM created to detect the node is Branch or Leaf
	static enum NodeType 
	{
		BRANCH, LEAF
	};

	public static void main(String[] args) 
	{

		// TODO Auto-generated method stub
		// String attributes = " ",
		String s = " ", s1 = " ", attributes = " ";
		FileInputStream training = null, validation = null;
		BufferedReader trainingfile = null, validationfile = null;
		int total = 0, attrlen = 0, yes = 0, no = 0, totalsize = 0, cntE = 0, cntV = 0, validationtotal = 0;
		double entropy = 0, VI = 0;

		// ArrayList to store the file data
		ArrayList<String[]> trainingdata = new ArrayList<String[]>();
		ArrayList<String> usedattributesE = new ArrayList<String>();
		ArrayList<String> usedattributesV = new ArrayList<String>();
		// ArrayList<String[]> validationdata = new ArrayList<String[]>();

		int L = Integer.parseInt(args[0]);
		int K = Integer.parseInt(args[1]);
		String trainingSet = args[2]; // filePath + "training_set1.csv";
		String testSet = args[3];// filePath + "test_set1.csv";
		String validationSet = args[4];// filePath + "validation_set1.csv";
		String toPrint = args[5];

		try {
			// Read Training Data and store in ArrayList
			training = new FileInputStream(trainingSet);
			trainingfile = new BufferedReader(new InputStreamReader(training));

			attributes = trainingfile.readLine();
			String line = " ";

			while ((line = trainingfile.readLine()) != null) {
				trainingdata.add(line.split(","));
			}
			total = trainingdata.size();
			System.out.println("Values is" + attributes);
			String[] atr = attributes.split(",");

			// Calculate Base Entropy
			double p1 = 0, p2 = 0;
			for (String[] record : trainingdata) {
				int l = record.length - 1;
				if (Integer.parseInt(record[l]) == 1) {
					no++;
				} else {
					yes++;
				}
			}
			p1 = (double) no / total;
			p2 = (double) yes / total;

			entropy = (-((p1 * (Math.log(p1)) / (Math.log(2)))) - ((p2
					* (Math.log(p2)) / (Math.log(2)))));

			// To Build Tree on Entropy
			Node Root = null;

			Root = BuildTree(trainingdata, usedattributesE, entropy, atr, Root,-1);

			// To Build Tree on Variance
			Node RootV = null;
			VI = ((double) (total - no) / total)
					* ((double) (total - yes) / total);
			RootV = BuildVarianceTree(trainingdata, usedattributesV, VI, atr,
					RootV, -1);

			// If Values is 'Y' then print tree else cannot go ahead
			if (toPrint.charAt(0) == 'Y') 
			{
				//Entropy
				System.out.println("***************Decision Tree by Entropy***************");
				inOrder(Root, s);
				System.out.println("Accuracy by Entropy on test set is"+AccuracyPercent(testSet, Root));
				System.out.println("***************Accuracy by Post Pruning using Entropy***************");		
				Node Dbest = postpruning(L, K, validationSet, Root);
				String x = " ";
				inOrder(Dbest, x);
				
				//Variance
				System.out.println("***************Decision Tree by Variance***************");
				inOrder(RootV, s1);
				System.out.println("Accuracy by Variance on test set is "+ AccuracyPercent(testSet, RootV));
				System.out.println();
				System.out.println("***************Accuracy by Post Pruning using Variance***************");
				Node Dbestf = postpruning(L, K, validationSet, RootV);
				String y = " ";
				inOrder(Dbestf, y);
	
			} 
			else 
			{	//Entropy
				System.out.println("***************Decision Tree by Entropy***************");
				System.out.println("***Accuracy by Entropy is"+AccuracyPercent(testSet, Root));
				System.out.println("***************Accuracy by Post Pruning using Entropy***************");
				Node Dbest = postpruning(L, K, validationSet, Root);
				
				//Variance
				System.out.println("***************Decision Tree by Variance***************");
				System.out.println("***Accuracy by Variance is "+ AccuracyPercent(testSet, RootV));
				System.out.println("***************Accuracy by Post Pruning using Variance***************");
				Node Dbestf = postpruning(L, K, validationSet, RootV);
				
				
			}
		} catch (FileNotFoundException e) // Handle File not Found error
		{
			System.out
					.println("Please make sure the directory file is actually there.");
		} catch (IOException ex) // Handle IO exception error
		{
			Logger.getLogger(BufferedReader.class.getName()).log(Level.SEVERE,
					null, ex);
		} finally {
			try {
				trainingfile.close();

			} catch (IOException ex) {
				Logger.getLogger(BufferedReader.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
	}

	// Post Pruning
	public static Node postpruning(int L, int K, String validationSet,
			Node finalroot) {
		Node Dbest = finalroot;
		int countnodes = 0;
		double accuracy = AccuracyPercent(validationSet, finalroot);
		System.out.println("***************Accuracy on Validation Set before Post Pruning is : " + accuracy);
		double accuracyBest = 0;
		for (int i = 0; i < L; i++) {
			Node Dtemp = null;
			Dtemp = copytree(finalroot, Dtemp);
			int M = (1 + (int) (Math.random() * K));

			for (int j = 0; j < M; j++) {
				int P = 0;
				List<Node> nodeList = new ArrayList<>();
				nodeList = arraypattern(nodeList, Dtemp);
				countnodes = nodeList.size() - 2;

				P = (1 + (int) (Math.random() * countnodes));
			
				if (P != 0 && nodeList.size() >= 2) {
					Node replace = nodeList.get(P);
					replace.leftchild = null;
					replace.rightchild = null;
					replace.type = NodeType.LEAF;
					if (replace.positivecount > replace.negativecount) {
						replace.attributeName = "1";
					} else {
						replace.attributeName = "0";
					}
					
				}

			}

			accuracyBest = AccuracyPercent(validationSet, Dtemp);
			if (accuracyBest > accuracy) {
				accuracy = accuracyBest;
				Dbest = Dtemp;
				// Dtemp = null;
			}
		}

		System.out.println("***************Best accuracy is : " + accuracy);
		return Dbest;
	}

	// Store nodes in Link List
	public static List<Node> arraypattern(List<Node> nodeList, Node finalroot) {

		if (finalroot != null && finalroot.type == NodeType.LEAF) {
			return nodeList;

		} else if (finalroot != null) {
			nodeList.add(finalroot);
			arraypattern(nodeList, finalroot.leftchild);
			arraypattern(nodeList, finalroot.rightchild);
		}
		return nodeList;
	}

	public static int countNumber(Node Dtemp, int count) {

		if (Dtemp.type != NodeType.LEAF) {
			count++;
			count = countNumber(Dtemp.leftchild, count);
			count = countNumber(Dtemp.rightchild, count);
		}
		return count;

	}

	// Copy tree to perform Post pruning
	public static Node copytree(Node finalroot, Node Dtemp) {
		if (finalroot.type == NodeType.LEAF) {
			Dtemp = new Node(finalroot.type, finalroot.entropyfinal,
					finalroot.attributeName, finalroot.value,
					finalroot.leftchild, finalroot.rightchild, 0, 0);

		} else {
			Dtemp = new Node(finalroot.type, finalroot.entropyfinal,
					finalroot.attributeName, finalroot.value,
					null, null,
					finalroot.positivecount, finalroot.negativecount);
			Dtemp.leftchild = copytree(finalroot.leftchild, Dtemp.leftchild);
			Dtemp.rightchild = copytree(finalroot.rightchild, Dtemp.rightchild);
		}

		return Dtemp;
	}

	// Calculate Accuracy Percent
	public static double AccuracyPercent(String test, Node Rootcal) {
		FileInputStream training = null, testset = null;
		BufferedReader trainingfile = null, testfile = null;
		String attributes = " ";
		double accuracy = 0;
		int atrslen = 0, totalsize = 0, cnt = 0;
		try {
			testset = new FileInputStream(test);
			testfile = new BufferedReader(new InputStreamReader(testset));
			attributes = testfile.readLine();
			String[] atrs = attributes.split(",");
			atrslen = atrs.length;
			String[] test_set = new String[atrs.length];
			HashMap<String, Integer> attributeindex = new HashMap<String, Integer>();
			for (int i = 0; i < atrs.length; i++) {
				attributeindex.put(atrs[i], i);
			}
			String line1 = " ";
			int x = 0, y = 0;
			while ((line1 = testfile.readLine()) != null) {

				test_set = line1.split(",");
				x = Accuracy(Rootcal, test_set, attributeindex);
				if (Integer.parseInt(test_set[atrs.length - 1]) == x) {
					cnt++;
				}
				totalsize++;
			}
			accuracy = ((double) cnt / totalsize) * 100;
		} catch (FileNotFoundException e) {
			System.out
					.println("Please make sure the directory file is actually there.");
		} catch (IOException ex) {
			Logger.getLogger(BufferedReader.class.getName()).log(Level.SEVERE,
					null, ex);
		} finally {
			try {
				testfile.close();
			} catch (IOException ex) {
				Logger.getLogger(BufferedReader.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
		return accuracy;
	}

	// Decision tree for Entropy
	public static Node BuildTree(ArrayList<String[]> records,
			ArrayList<String> usedattributesE, double entropy, String atr[],
			Node nodeE, int value) {

		ArrayList<String[]> arrayleft = new ArrayList<String[]>();
		ArrayList<String[]> arrayright = new ArrayList<String[]>();

		ArrayList<String> newattributesE = new ArrayList<String>(
				usedattributesE);

		double entropyY = 0, entropyN = 0, Gain = 0, maxentropyY = 0, maxentropyN = 0, maxpos = 0, maxneg = 0;
		int i = 0, maxindex = 0;

		Double max = Double.valueOf(Double.NEGATIVE_INFINITY);
//The leaf node attribute name conatins the final value
		if (newattributesE.size() == atr.length - 1 || records.isEmpty()) {
			return new Node(NodeType.LEAF, entropy, "" + value, value);
		} else if (entropy == 0) {
			return new Node(NodeType.LEAF, entropy,
					records.get(0)[records.get(0).length - 1], value);

		} else {

			while (i != atr.length - 1) {
				double attrYclassY = 0, attrYclassN = 0, attrNclassY = 0, attrNclassN = 0, negative = 0, positive = 0;

				if (!newattributesE.contains(atr[i])) {
					for (String[] ln : records) {
						int l = ln.length - 1;

						if (Integer.parseInt(ln[i]) == 0)
							negative++;
						else
							positive++;

						if (Integer.parseInt(ln[l]) == 1) { // yes pos examples
							if (Integer.parseInt(ln[i]) == 1) {
								attrYclassY++;
							} else {
								attrNclassY++;
							}
						} else {
							if (Integer.parseInt(ln[i]) == 1) {
								attrYclassN++;
							} else {
								attrNclassN++;
							}
						}
					}

					if (attrYclassY == 0 || attrYclassN == 0) {
						entropyY = 0;
					} else {
						entropyY = -((attrYclassY / (attrYclassY + attrYclassN)) * ((Math
								.log(attrYclassY / (attrYclassY + attrYclassN))) / (Math
								.log(2))))
								- ((attrYclassN / (attrYclassY + attrYclassN)) * ((Math
										.log(attrYclassN
												/ (attrYclassN + attrYclassY))) / (Math
										.log(2))));
					}

					if (attrNclassY == 0 || attrNclassN == 0) {
						entropyN = 0;
					} else {
						entropyN = -((attrNclassY / (attrNclassY + attrNclassN)) * ((Math
								.log(attrNclassY / (attrNclassY + attrNclassN))) / (Math
								.log(2))))
								- ((attrNclassN / (attrNclassY + attrNclassN)) * ((Math
										.log(attrNclassN
												/ (attrNclassY + attrNclassN))) / (Math
										.log(2))));
					}

					Gain = entropy
							- (((positive / (positive + negative)) * entropyY))
							- (((negative / (positive + negative)) * entropyN));

					if (max < Gain) {
						max = Gain;
						maxindex = i;
						maxentropyY = entropyY;
						maxentropyN = entropyN;
						maxpos = attrYclassY + attrNclassY;
						maxneg = attrYclassN + attrNclassN;
					}

				}

				i++;

			}

			nodeE = new Node(NodeType.BRANCH, entropy, atr[maxindex], value,
					maxpos, maxneg);

			newattributesE.add(atr[maxindex]);

			for (String[] record : records) {
				if (Integer.parseInt(record[maxindex]) == 1) {
					arrayright.add(record);
				} else {
					arrayleft.add(record);
				}
			}

		}
		nodeE.leftchild = BuildTree(arrayleft, newattributesE, maxentropyN,
				atr, nodeE.leftchild, 0);
		nodeE.rightchild = BuildTree(arrayright, newattributesE, maxentropyY,
				atr, nodeE.rightchild, 1);
		return nodeE;
	}

	// Decision Tree for Variance
	public static Node BuildVarianceTree(ArrayList<String[]> recordV,
			ArrayList<String> usedattributesV, double VI, String atr[],
			Node nodeV, int value) {
		ArrayList<String[]> arrayleft = new ArrayList<String[]>();
		ArrayList<String[]> arrayright = new ArrayList<String[]>();

		ArrayList<String> newattributesV = new ArrayList<String>(
				usedattributesV);

		double vi_Y = 0, vi_N = 0, Gain = 0, varianceY = 0, varianceN = 0, maxpos = 0, maxneg = 0;
		int i = 0, maxindex = 0;

		Double max = Double.valueOf(Double.NEGATIVE_INFINITY);

		if (newattributesV.size() == atr.length - 1 || recordV.isEmpty()) {
			return new Node(NodeType.LEAF, VI, "" + value, value);
		} else if (VI == 0) {
			return new Node(NodeType.LEAF, VI,
					recordV.get(0)[recordV.get(0).length - 1], value);

		} else {

			while (i != atr.length - 1) {
				double attrYclassY = 0, attrYclassN = 0, attrNclassY = 0, attrNclassN = 0, negative = 0, positive = 0;

				if (!newattributesV.contains(atr[i])) {
					for (String[] ln : recordV) {
						int l = ln.length - 1;

						if (Integer.parseInt(ln[i]) == 0)
							negative++;
						else
							positive++;

						if (Integer.parseInt(ln[l]) == 1) { // yes pos examples
							if (Integer.parseInt(ln[i]) == 1) {
								attrYclassY++;
							} else {
								attrNclassY++;
							}
						} else {
							if (Integer.parseInt(ln[i]) == 1) {
								attrYclassN++;
							} else {
								attrNclassN++;
							}
						}
					}

					if (attrYclassY == 0 || attrYclassN == 0) {
						vi_Y = 0;
					} else {
						vi_Y = ((double) attrYclassY / (attrYclassY + attrYclassN))
								* ((double) attrYclassN / (attrYclassY + attrYclassN));
					}

					if (attrNclassY == 0 || attrNclassN == 0) {
						vi_N = 0;
					} else {
						vi_N = ((double) attrNclassY / (attrNclassY + attrNclassN))
								* ((double) attrNclassN / (attrNclassY + attrNclassN));
					}

					Gain = VI
							- ((((double) positive / (positive + negative)) * vi_Y))
							- ((((double) negative / (positive + negative)) * vi_N));

					if (max < Gain) {
						max = Gain;
						maxindex = i;
						varianceY = vi_Y;
						varianceN = vi_N;
						maxpos = attrYclassY + attrNclassY;
						maxneg = attrYclassN + attrNclassN;
					}

				}

				i++;

			}

			nodeV = new Node(NodeType.BRANCH, VI, atr[maxindex], value, maxpos,
					maxneg);

			newattributesV.add(atr[maxindex]);

			for (String[] record : recordV) {
				if (Integer.parseInt(record[maxindex]) == 1) {
					arrayright.add(record);
				} else {
					arrayleft.add(record);
				}
			}

		}

		nodeV.leftchild = BuildTree(arrayleft, newattributesV, varianceN, atr,
				nodeV.leftchild, 0);

		nodeV.rightchild = BuildTree(arrayright, newattributesV, varianceY,
				atr, nodeV.rightchild, 1);
		return nodeV;
	}

	// Create Node to form a tree
	static class Node {
		int value;
		double positivecount = 0;
		double negativecount = 0;
		String attributeName;
		NodeType type;
		double entropyfinal;
		Node leftchild = null;
		Node rightchild = null;

		// ArrayList<Node> nodechildren = null;

		public Node(NodeType Type, Double Entropy, String attr, int value) {
			this.type = Type;
			this.entropyfinal = Entropy;
			Node leftchild = null;
			Node rightchild = null;
			this.attributeName = attr;
			this.value = value;
		}

		public Node(NodeType Type, Double Entropy, String attr, int value,
				Node leftchild, Node rightchild, double positivecount,
				double negativecount) {
			this.type = Type;
			this.entropyfinal = Entropy;
			this.leftchild = leftchild;
			this.rightchild = rightchild;
			this.attributeName = attr;
			this.value = value;
			this.positivecount = positivecount;
			this.negativecount = negativecount;
		}

		public Node(NodeType Type, Double Entropy, String attr, int value,
				double positivecount, double negativecount) {
			this.type = Type;
			this.entropyfinal = Entropy;
			this.attributeName = attr;
			this.value = value;
			this.positivecount = positivecount;
			this.negativecount = negativecount;
		}

		public Node() {
		}

		public Node leftNode() {
			return leftchild;
		}

		public Node rightNode() {
			return rightchild;
		}

		public String toString() {
			// TODO Auto-generated method stub
			if (type == NodeType.BRANCH) {
				return attributeName + value;
			} else {
				return " " + value;
			}
		}

	}

	// To print Tree
	public static void inOrder(Node node, String s) {

		if (node == null)
			return;
		else if (node.type == NodeType.LEAF) {
			System.out.println(": " + node.attributeName);
		} else if (node.type == NodeType.BRANCH) {
			System.out.println(" ");

			if (node.leftchild != null) {
				System.out.print(" | " + s);
				System.out.print(node.attributeName + " = "
						+ node.leftchild.value);
				inOrder(node.leftNode(), s + " | ");
			}
			if (node.rightchild != null) {
				System.out.print(" | " + s);
				System.out.print(node.attributeName + " = "
						+ node.rightchild.value);
				inOrder(node.rightNode(), s + " | ");
			}
		}
	}

	// Calculate Accuracy
	public static int Accuracy(Node node, String[] test_set,
			HashMap<String, Integer> attributeindex) {
		if (node.type == NodeType.LEAF) {

			return Integer.parseInt(node.attributeName);
		}

		String name = " ";
		name = node.attributeName;
		int index = attributeindex.get(name);

		if (Integer.parseInt(test_set[index]) == 0) {

			return Accuracy(node.leftchild, test_set, attributeindex);

		} else {
			return Accuracy(node.rightchild, test_set, attributeindex);
		}

	}
}