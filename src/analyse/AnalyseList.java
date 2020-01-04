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

	// 成员变量,产生式集，终结符集，非终结符集
	ArrayList<Production> productions;
	ArrayList<String> terminals;
	ArrayList<String> nonterminals;

	HashMap<String, ArrayList<String>> firsts;
	HashMap<String, ArrayList<String>> follows;

//	String type;// 节点类型
//	String name;// 节点名
//	String value;// 节点值

	// 添加动作标志


//	public AnalyseList(String type, String name, String value) {
//		this.type = type;
//		this.name = name;
//		this.value = value;
//	}

	public AnalyseList() {
		productions = new ArrayList<Production>(); // 产生式集
		terminals = new ArrayList<String>(); // 终结符集
		nonterminals = new ArrayList<String>(); // 非终结符集
		firsts = new HashMap<String, ArrayList<String>>();// first集
		follows = new HashMap<String, ArrayList<String>>();// follow集
		setProductions();
		getFirst();
		getFollow();
		getSelect();
		Predict();
	}

	// 从文件中读取产生式,并获取非终结符和终结符
	public void setProductions() {
		try {
			File file = new File("myG.txt");
			RandomAccessFile randomfile = new RandomAccessFile(file, "r");
			String line;
			String left; // 产生式左边
			String right; // 产生式右边
			String[] rights; // 将产生式右边保存为以“ ”空格为分割符后形成的字符串数组
			Production production;
			while ((line = randomfile.readLine()) != null) {
				left = line.split("->")[0].trim(); // 以“->”分割后的字符串数组中的第一个为非终结符
				right = line.split("->")[1].trim();
				rights = right.split(" ");
				production = new Production(left, rights);
				productions.add(production);

				// 获取非终结符
				// if(actionSign.contains(left))
				if (nonterminals.contains(left)) {
					continue;
				} else {
					nonterminals.add(left);
				}

			}
			// 获取终结符
			String[] rights1;
			for (int i = 0; i < productions.size(); i++) {
				rights1 = productions.get(i).returnRights();
				// 从右侧寻找终结符
				for (int j = 0; j < rights1.length; j++) {
					if (nonterminals.contains(rights1[j]) || rights1[j].equals("$")) { // 若右侧中包含非终结符和$空集，则继续
						continue;
					} else {
						terminals.add(rights1[j]); // 否则将终结符加入终结符集中
					}
				}
			}
			randomfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 获取First集
	public void getFirst() {
		// 终结符全部求出first集
		ArrayList<String> first; // 用于存放first集
		// 给所有非终结符注册一下，初始化为空
		for (int i = 0; i < nonterminals.size(); i++) {
			first = new ArrayList<String>();
			firsts.put(nonterminals.get(i), first);
		}
		boolean flag; //跳出循环的标志
		while (true) {
			flag = true;
			String left;
			String right;// 用于遍历产生式右边字符串数组的标志
			String[] rights;
			for (int i = 0; i < productions.size(); i++) {
				left = productions.get(i).returnLeft();// 第i条产生式的左边
				rights = productions.get(i).returnRights();// 第i条产生式的右边
				//判断空情况
				for (int j = 0; j < rights.length; j++) {// 遍历产生式右边的字符串数组
					right = rights[j];
					if (!right.equals("$")) {
						//当 右侧第一个为终结符时
						if (isTerminal(right)) {
							if (!firsts.get(left).contains(right)) {
								firsts.get(left).add(right);
								flag = false;
							}
							break;
							//当right为非终结符时
						} else if (isNonterminals(right)){
							for (int l = 0; l < firsts.get(right).size(); l++) {
								if (firsts.get(left).contains(firsts.get(right).get(l))) {// 第i条产生式的非终结符的first集是否包含产生式右边的first集里的第l个终结符
									continue;
								} else if(!firsts.get(right).get(l).equals("$")||j == rights.length -1){ //不包含就加入first集里
									firsts.get(left).add(firsts.get(right).get(l));
									flag = false;
								}
							}
							//如果为空则，判断下一个right，不为空则判断下一个产生式
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

	// 获得产生式右边所有非终结符的Follow集
	public void getFollow() {
		ArrayList<String> first; // 用于存放first集
		//终结符的first集等于自身 用于求follow集用
		for (int i = 0; i < terminals.size(); i++) {
			first = new ArrayList<String>();
			first.add(terminals.get(i));
			firsts.put(terminals.get(i), first);
		}
		// 所有非终结符的follow集初始化一下
		ArrayList<String> follow;
		for (int i = 0; i < nonterminals.size(); i++) {
			follow = new ArrayList<String>();
			follows.put(nonterminals.get(i), follow);
		}
		// 将#加入到follow(S)中
		follows.get("S").add("#");
		boolean flag;
		boolean fab;
		while (true) {
			flag = true; //跳出while循环的标志
			// 循环
			for (int i = 0; i < productions.size(); i++) {
				String left;
				String right;
				String[] rights;
				rights = productions.get(i).returnRights();
				for (int j = 0; j < rights.length; j++) {
					right = rights[j];
					// 产生式右边找到非终结符,求非终结符之后的所有每个的first集，若为空则，找下一个U->xAy
					if (nonterminals.contains(right)) {
						fab = true; //右边非终结符的后一个元素可产生空集的标志
						for (int k = j + 1; k < rights.length; k++) {
							// 查找first集
							for (int v = 0; v < firsts.get(rights[k]).size(); v++) {
								// 将后一个元素的first集加入到前一个元素的follow集中
								if (follows.get(right).contains(firsts.get(rights[k]).get(v))) {
									continue;
									//去空的过程
								} else if(!firsts.get(rights[k]).get(v).equals("$")){
									follows.get(right).add(firsts.get(rights[k]).get(v));
									flag = false;
								}
							}
							//判断 是否为 u->xA形式
							if (isCanBeNull(rights[k])) {
								continue;
							} else {
								fab = false;
								break;
							}
						}
						//右侧都为空 U->xA
						if (fab) {
							left = productions.get(i).returnLeft(); // 所求的产生式的左部
							for (int p = 0; p < follows.get(left).size(); p++) { //遍历产生式左边的非终结符的follow集
								if (follows.get(right).contains(follows.get(left).get(p))) {
									continue; //若右边的非终结符的follow集包含产生式左边的非终结符的follow集中的元素，则继续分析
								} else {
									follows.get(right).add(follows.get(left).get(p)); //否则，加入到follow集中
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

	// 获取Select集
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
				// 若求select(A->$)则select(i) = follow(A),i=A->$
				follow = follows.get(left);
				for (int j = 0; j < follow.size(); j++) {
					if (productions.get(i).select.contains(follow.get(j))) {
						continue;
					} else {
						productions.get(i).select.add(follow.get(j));
					}
				}
			} else { // 若求select(A->B),则先求first(B),i=A->B
				boolean flag = true; //B产生空集的标志
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
					if (isCanBeNull(right)) { // 如果B的产生式中存在B->$，继续分析
						continue;
					} else { // 若B的产生式中不存在B->$,则flag=false
						flag = false;
						break;
					}
				}
				if (flag) { // select(A->B),若在B的产生式中存在B->$,则 =first(B)-{$} + follow(A)
					follow = follows.get(left); // A的follow集
					for (int j = 0; j < follow.size(); j++) {
						if (productions.get(i).select.contains(follow.get(j))) {
							continue;
						} else {
							// 将A的follow集添加到select中
							productions.get(i).select.add(follow.get(j));
						}
					}

				}
			}
		}
	}

	// 输出预测分析表
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
				for (int j = 0; j < production.select.size(); j++) { //遍历产生式的select集
					line = production.returnLeft() + "@" + production.select.get(j) + " ->";
					rights = production.returnRights();
					for (int v = 0; v < rights.length; v++) {//遍历产生式的右边
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

	// 判断是否是终结符
	public boolean isTerminal(String symbol) {
		return terminals.contains(symbol);
	}

	// 判断是否是非终结符
	public boolean isNonterminals(String symbol) {
		return nonterminals.contains(symbol);
	}

	// 判断是否是动作符号
//	public boolean isActionSign(String symbol) {
//		return actionSign.contains(symbol);
//	}

	// 判断是否产生$空集
	public boolean isCanBeNull(String symbol) {
		String[] rights;
		String right;
		//boolean flag = false; //空集标志
		for (int i = 0; i < productions.size(); i++) {
			// 找到产生式
			if (productions.get(i).returnLeft().equals(symbol)) {
				rights = productions.get(i).returnRights();
				for(int j = 0;j < rights.length;j++) {
					right = rights[j];
					if(right.equals("$")) {
						return true;
						//非终结符
					}else if(isNonterminals(right)) {
						//不能推出空则找下一个产生式
						if(!isCanBeNull(right)) {
							break;
							//能推出空并且为最后一个right，则一定可以推出空
						}else if(j == rights.length - 1) {
							return true;
						}
						//终结符 判断下一个产生式
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