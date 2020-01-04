package analyse;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import entity.Production;

public class AnalyseList {

	// ��Ա����,����ʽ�����ս���������ս����
	ArrayList<Production> productions;
	ArrayList<String> terminals;
	ArrayList<String> nonterminals;

	HashMap<String, ArrayList<String>> firsts;
	HashMap<String, ArrayList<String>> follows;

//	String type;// �ڵ�����
//	String name;// �ڵ���
//	String value;// �ڵ�ֵ

	// ��Ӷ�����־


//	public AnalyseList(String type, String name, String value) {
//		this.type = type;
//		this.name = name;
//		this.value = value;
//	}

	public AnalyseList() {
		productions = new ArrayList<Production>(); // ����ʽ��
		terminals = new ArrayList<String>(); // �ս����
		nonterminals = new ArrayList<String>(); // ���ս����
		firsts = new HashMap<String, ArrayList<String>>();// first��
		follows = new HashMap<String, ArrayList<String>>();// follow��
		setProductions();
		getFirst();
		getFollow();
		getSelect();
		Predict();
	}

	// ���ļ��ж�ȡ����ʽ,����ȡ���ս�����ս��
	public void setProductions() {
		try {
			File file = new File("myG.txt");
			RandomAccessFile randomfile = new RandomAccessFile(file, "r");
			String line;
			String left; // ����ʽ���
			String right; // ����ʽ�ұ�
			String[] rights; // ������ʽ�ұ߱���Ϊ�ԡ� ���ո�Ϊ�ָ�����γɵ��ַ�������
			Production production;
			while ((line = randomfile.readLine()) != null) {
				left = line.split("->")[0].trim(); // �ԡ�->���ָ����ַ��������еĵ�һ��Ϊ���ս��
				right = line.split("->")[1].trim();
				rights = right.split(" ");
				production = new Production(left, rights);
				productions.add(production);

				// ��ȡ���ս��
				// if(actionSign.contains(left))
				if (nonterminals.contains(left)) {
					continue;
				} else {
					nonterminals.add(left);
				}

			}
			// ��ȡ�ս��
			String[] rights1;
			for (int i = 0; i < productions.size(); i++) {
				rights1 = productions.get(i).returnRights();
				// ���Ҳ�Ѱ���ս��
				for (int j = 0; j < rights1.length; j++) {
					if (nonterminals.contains(rights1[j]) || rights1[j].equals("$")) { // ���Ҳ��а������ս����$�ռ��������
						continue;
					} else {
						terminals.add(rights1[j]); // �����ս�������ս������
					}
				}
			}
			randomfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// ��ȡFirst��
	public void getFirst() {
		// �ս��ȫ�����first��
		ArrayList<String> first; // ���ڴ��first��
		// �����з��ս��ע��һ�£���ʼ��Ϊ��
		for (int i = 0; i < nonterminals.size(); i++) {
			first = new ArrayList<String>();
			firsts.put(nonterminals.get(i), first);
		}
		boolean flag; //����ѭ���ı�־
		while (true) {
			flag = true;
			String left;
			String right;// ���ڱ�������ʽ�ұ��ַ�������ı�־
			String[] rights;
			for (int i = 0; i < productions.size(); i++) {
				left = productions.get(i).returnLeft();// ��i������ʽ�����
				rights = productions.get(i).returnRights();// ��i������ʽ���ұ�
				//�жϿ����
				for (int j = 0; j < rights.length; j++) {// ��������ʽ�ұߵ��ַ�������
					right = rights[j];
					if (!right.equals("$")) {
						//�� �Ҳ��һ��Ϊ�ս��ʱ
						if (isTerminal(right)) {
							if (!firsts.get(left).contains(right)) {
								firsts.get(left).add(right);
								flag = false;
							}
							break;
							//��rightΪ���ս��ʱ
						} else if (isNonterminals(right)){
							for (int l = 0; l < firsts.get(right).size(); l++) {
								if (firsts.get(left).contains(firsts.get(right).get(l))) {// ��i������ʽ�ķ��ս����first���Ƿ��������ʽ�ұߵ�first����ĵ�l���ս��
									continue;
								} else if(!firsts.get(right).get(l).equals("$")||j == rights.length -1){ //�������ͼ���first����
									firsts.get(left).add(firsts.get(right).get(l));
									flag = false;
								}
							}
							//���Ϊ�����ж���һ��right����Ϊ�����ж���һ������ʽ
							if(!isCanBeNull(right))
								break;
						}
					}else{
						if(!firsts.get(left).contains("$")){
							firsts.get(left).add("$");
							flag = false;
						}
					}
				}
			}
			if (flag == true) {
				break;
			}
		}
	}

	// ��ò���ʽ�ұ����з��ս����Follow��
	public void getFollow() {
		ArrayList<String> first; // ���ڴ��first��
		//�ս����first���������� ������follow����
		for (int i = 0; i < terminals.size(); i++) {
			first = new ArrayList<String>();
			first.add(terminals.get(i));
			firsts.put(terminals.get(i), first);
		}
		// ���з��ս����follow����ʼ��һ��
		ArrayList<String> follow;
		for (int i = 0; i < nonterminals.size(); i++) {
			follow = new ArrayList<String>();
			follows.put(nonterminals.get(i), follow);
		}
		// ��#���뵽follow(S)��
		follows.get("S").add("#");
		boolean flag;
		boolean fab;
		while (true) {
			flag = true; //����whileѭ���ı�־
			// ѭ��
			for (int i = 0; i < productions.size(); i++) {
				String left;
				String right;
				String[] rights;
				rights = productions.get(i).returnRights();
				for (int j = 0; j < rights.length; j++) {
					right = rights[j];
					// ����ʽ�ұ��ҵ����ս��,����ս��֮�������ÿ����first������Ϊ��������һ��U->xAy
					if (nonterminals.contains(right)) {
						fab = true; //�ұ߷��ս���ĺ�һ��Ԫ�ؿɲ����ռ��ı�־
						for (int k = j + 1; k < rights.length; k++) {
							// ����first��
							for (int v = 0; v < firsts.get(rights[k]).size(); v++) {
								// ����һ��Ԫ�ص�first�����뵽ǰһ��Ԫ�ص�follow����
								if (follows.get(right).contains(firsts.get(rights[k]).get(v))) {
									continue;
									//ȥ�յĹ���
								} else if(!firsts.get(rights[k]).get(v).equals("$")){
									follows.get(right).add(firsts.get(rights[k]).get(v));
									flag = false;
								}
							}
							//�ж� �Ƿ�Ϊ u->xA��ʽ
							if (isCanBeNull(rights[k])) {
								continue;
							} else {
								fab = false;
								break;
							}
						}
						//�Ҳ඼Ϊ�� U->xA
						if (fab) {
							left = productions.get(i).returnLeft(); // ����Ĳ���ʽ����
							for (int p = 0; p < follows.get(left).size(); p++) { //��������ʽ��ߵķ��ս����follow��
								if (follows.get(right).contains(follows.get(left).get(p))) {
									continue; //���ұߵķ��ս����follow����������ʽ��ߵķ��ս����follow���е�Ԫ�أ����������
								} else {
									follows.get(right).add(follows.get(left).get(p)); //���򣬼��뵽follow����
									flag = false;
								}
							}
						}
					}

				}
			}
			if (flag == true) {
				break;
			}
		}
	}

	// ��ȡSelect��
	public void getSelect() {
		String left;
		String right;
		String[] rights;
		ArrayList<String> follow = new ArrayList<String>();
		ArrayList<String> first = new ArrayList<String>();

		for (int i = 0; i < productions.size(); i++) {
			left = productions.get(i).returnLeft();
			rights = productions.get(i).returnRights();
			if (rights[0].equals("$")) {
				// ����select(A->$)��select(i) = follow(A),i=A->$
				follow = follows.get(left);
				for (int j = 0; j < follow.size(); j++) {
					if (productions.get(i).select.contains(follow.get(j))) {
						continue;
					} else {
						productions.get(i).select.add(follow.get(j));
					}
				}
			} else { // ����select(A->B),������first(B),i=A->B
				boolean flag = true; //B�����ռ��ı�־
				for (int j = 0; j < rights.length; j++) {
					right = rights[j];
					first = firsts.get(right);
					for (int v = 0; v < first.size(); v++) {
						if (productions.get(i).select.contains(first.get(v))) {
							continue;
						} else if(!first.get(v).equals("$")){
							productions.get(i).select.add(first.get(v));
						}
					}
					if (isCanBeNull(right)) { // ���B�Ĳ���ʽ�д���B->$����������
						continue;
					} else { // ��B�Ĳ���ʽ�в�����B->$,��flag=false
						flag = false;
						break;
					}
				}
				if (flag) { // select(A->B),����B�Ĳ���ʽ�д���B->$,�� =first(B)-{$} + follow(A)
					follow = follows.get(left); // A��follow��
					for (int j = 0; j < follow.size(); j++) {
						if (productions.get(i).select.contains(follow.get(j))) {
							continue;
						} else {
							// ��A��follow����ӵ�select��
							productions.get(i).select.add(follow.get(j));
						}
					}

				}
			}
		}
	}

	// ���Ԥ�������
	public void Predict() {
		Production production;
		String line;
		String[] rights;
		try {
			FileOutputStream fos = new FileOutputStream("predictldy.txt");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw1 = new OutputStreamWriter(bos);
			PrintWriter pw1 = new PrintWriter(osw1);
			//File file = new File("predictldy.txt");
			//RandomAccessFile randomfile = new RandomAccessFile(file, "rw");
			for (int i = 0; i < productions.size(); i++) {
				production = productions.get(i);
				for (int j = 0; j < production.select.size(); j++) { //��������ʽ��select��
					line = production.returnLeft() + "@" + production.select.get(j) + " ->";
					rights = production.returnRights();
					for (int v = 0; v < rights.length; v++) {//��������ʽ���ұ�
						line = line + " " + rights[v];
					}
					//line = line + "\n";

					pw1.println(line);
				}
			}
			pw1.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	// �ж��Ƿ����ս��
	public boolean isTerminal(String symbol) {
		return terminals.contains(symbol);
	}

	// �ж��Ƿ��Ƿ��ս��
	public boolean isNonterminals(String symbol) {
		return nonterminals.contains(symbol);
	}

	// �ж��Ƿ��Ƕ�������
//	public boolean isActionSign(String symbol) {
//		return actionSign.contains(symbol);
//	}

	// �ж��Ƿ����$�ռ�
	public boolean isCanBeNull(String symbol) {
		String[] rights;
		String right;
		//boolean flag = false; //�ռ���־
		for (int i = 0; i < productions.size(); i++) {
			// �ҵ�����ʽ
			if (productions.get(i).returnLeft().equals(symbol)) {
				rights = productions.get(i).returnRights();
				for(int j = 0;j < rights.length;j++) {
					right = rights[j];
					if(right.equals("$")) {
						return true;
						//���ս��
					}else if(isNonterminals(right)) {
						//�����Ƴ���������һ������ʽ
						if(!isCanBeNull(right)) {
							break;
							//���Ƴ��ղ���Ϊ���һ��right����һ�������Ƴ���
						}else if(j == rights.length - 1) {
							return true;
						}
						//�ս�� �ж���һ������ʽ
					}else {
						break;
					}
				}
			}
		}
		return false;
	}


	public void outputFirst() throws IOException{
		FileOutputStream fos = new FileOutputStream("first.txt");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		OutputStreamWriter osw1 = new OutputStreamWriter(bos);
		PrintWriter pw1 = new PrintWriter(osw1);
			for(int j=0;j<nonterminals.size();j++) {
				ArrayList<String> temp=new ArrayList<String>();
				temp=firsts.get(nonterminals.get(j));
				pw1.print(nonterminals.get(j)+" : ");
				for(int k=0;k<temp.size();k++)
					pw1.print(temp.get(k)+"  ");
				pw1.println();
			}
		pw1.close();
	}
	
	public void outputFollow() throws IOException{
		FileOutputStream fos = new FileOutputStream("follow.txt");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		OutputStreamWriter osw1 = new OutputStreamWriter(bos);
		PrintWriter pw1 = new PrintWriter(osw1);
			for(int j=0;j<nonterminals.size();j++) {
				ArrayList<String> temp=new ArrayList<String>();
				temp=follows.get(nonterminals.get(j));
				pw1.print(nonterminals.get(j)+" : ");
				for(int k=0;k<temp.size();k++)
					pw1.print(temp.get(k)+"  ");
				pw1.println();
			}
		pw1.close();
	}
	
//	public static boolean isActionSign(AnalyseList node) {
//		return actionSign.contains(node.name);
//	}

	public static void main(String[] args) {
		AnalyseList list = new AnalyseList();
		list.getFirst();
		list.getFollow();
		list.getSelect();
	}
}