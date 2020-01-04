package analyse;

import entity.Words;
import entity.MyError;
import entity.FourElement;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class TextParse {
	static ArrayList<String> actionSign = new ArrayList<String>();// 动作符集合
	HashMap<String, String> predictmap; // 预测分析表
	ArrayList<String> input_cache; // 输入
	ArrayList<String> deduce_str; // 分析栈
	public ArrayList<MyError> myErrorList = new ArrayList<MyError>();
	int errorCount = 0;
	public ArrayList<FourElement> fourElemList = new ArrayList<FourElement>();// 四元式
	public ArrayList<Words> wordsList = new ArrayList<Words>(); // 单词表
	Words firstWord;// 待分析单词
	public boolean graErrorFlag = false;// 语义错误标志
	StringBuffer bf;// 分析栈缓冲流
	String OP = null; // 操作符
	String ARG1, ARG2, RES;
	int tempCount = 0;// 生成临时变量
	int fourElemCount = 0;// 四元式序号
	Stack<String> semanticStack = new Stack<String>();// 语义栈
	public ArrayList<String> fourElemT = new ArrayList<String>();
	Stack<Integer> if_fj = new Stack<Integer>();
	Stack<Integer> if_rj = new Stack<Integer>();;
	Stack<Integer> while_fj = new Stack<Integer>();;
	Stack<Integer> while_rj = new Stack<Integer>();;
	Stack<Integer> for_fj = new Stack<Integer>();;
	Stack<Integer> for_rj = new Stack<Integer>();;// if while for
	Stack<String> for_op = new Stack<String>();
	MyError myError;
	String L = "";
	String F = "";
	String T = "";
	String R = "";
	String Q = "";
	//String U = "";
	String D = "";
	String G = "";
	public final static String ACTIONSIGN = "动作符";
	static {
		actionSign.add("ADD_SUB");
		actionSign.add("ADD");
		actionSign.add("SUB");
		actionSign.add("DIV_MUL");
		actionSign.add("DIV");
		actionSign.add("MUL");
		actionSign.add("SINGLE");
//		actionSign.add("SINGTLE_OP");
		actionSign.add("SINGLE_OP");
		actionSign.add("ASS_R");
		actionSign.add("ASS_Q");
		actionSign.add("ASS_F");
		actionSign.add("SCANF");
		actionSign.add("TRAN_LF");
		actionSign.add("EQ");
		actionSign.add("PRINTF");
		actionSign.add("COMPARE");
		actionSign.add("COMPARE_OP");
		actionSign.add("IF_FJ");
		actionSign.add("IF_BACKPATCH_FJ");
		actionSign.add("IF_RJ");
		actionSign.add("IF_BACKPATCH_RJ");
		actionSign.add("WHILE_FJ");
		actionSign.add("WHILE_BACKPATCH_FJ");
		actionSign.add("WHILE_RJ");
		actionSign.add("IF_RJ");
		actionSign.add("FOR_FJ");
		actionSign.add("FOR_RJ");
		actionSign.add("FOR_BACKPATCH_FJ");
	}
	public TextParse(ArrayList<String> input_cache, LexAnalyse lexAnalyse) { // , DefaultTableModel tbmodel_lex_result
		predictmap = new HashMap<String, String>();
		this.input_cache = input_cache;
		// System.out.println(input_cache);
		this.wordsList = lexAnalyse.wordsList;
		deduce_str = new ArrayList<String>();
		getPredictMap(); //获取预测分析表
	}

	// 语法分析
	public void Parsing() {
		// 文法开始符入栈
		deduce_str.add("#");
		deduce_str.add("S");
		semanticStack.add("#"); // #入语义栈
		String right;
		String leftandinput;
		bf = new StringBuffer();
		int index = 0;// 下标

		bf.append("步骤\t\t\t分析栈\t\t\t\t\t\t\t\t剩余输入串\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t产生式\t\t\t\t\t\t\t语义栈");
		//如果输入的待分析的字符串和字符串分析栈都不为空，则开始语法分析
		while (deduce_str.size() > 0 && input_cache.size() > 0) {
			bf.append('\n');
			bf.append(index++ + "\t"); // 步骤

			firstWord = wordsList.get(0); // 待分析单词
			// if(input_cache.get(0).equals(deduce_str.get(deduce_str.size()-1))&&input_cache.get(index))
			//若剩余输入串中只有#时，语法分析成功
			if (input_cache.get(0).equals(deduce_str.get(deduce_str.size() - 1)) && input_cache.get(0).equals("#")) {
				bf.append("\t\t语法分析成功\n");
				//输出四元式或错误信息
				outputInfo();
				break;
			} else if (input_cache.get(0).equals(deduce_str.get(deduce_str.size() - 1))) {
				// 剩余输入串第一个字符与分析栈中第一个字符相等的话，输出产生式和推导过程
				bf.append("\t\t"); // 分析栈
				for (int i = deduce_str.size() - 1; i > -1; i--) {
					bf.append(deduce_str.get(i));
				}
				bf.append("\t\t\t\t\t"); // 剩余输入串
				for (int j = 0; j < wordsList.size(); j++) {
					bf.append(wordsList.get(j).value);
				}
				input_cache.remove(0);
				String pipei = deduce_str.remove(deduce_str.size() - 1); //删掉匹配的字符，将产生式右边的字符压入堆栈
				wordsList.remove(0);//单词表栈顶出栈
				bf.append("\t\t\t\t\t"); // 产生式
				bf.append(pipei + "匹配"); //匹配的字符
				
				//语义栈
				bf.append("\t\t\t");
				for (int j = 0; j < semanticStack.size(); j++) {
					bf.append(semanticStack.get(j));
				}
				continue;
			}
			// 构成S-int型的字符串，从预分析表中匹配字符，找到相应的产生式
			leftandinput = deduce_str.get(deduce_str.size() - 1) + "-" + input_cache.get(0); //
			// 能够找到匹配的S-int型字符
			if ((right = predictmap.get(leftandinput)) != null) {
				//如果分析栈中的第一个元素等于动作符时，开始产生四元式
				if (actionSign.contains(deduce_str.get(deduce_str.size() - 1)))
					actionSignOP(deduce_str.get(deduce_str.size() - 1));

				bf.append("\t\t"); // 分析栈
				for (int i = deduce_str.size() - 1; i > -1; i--) {
					bf.append(deduce_str.get(i));
				}
				bf.append("\t\t\t\t\t"); // 剩余输入串
				for (int j = 0; j < wordsList.size(); j++) {
					bf.append(wordsList.get(j).value);
				}
				bf.append("\t\t\t\t\t"); // 产生式
				bf.append(deduce_str.get(deduce_str.size() - 1) + "->" + right);
				
				bf.append("\t\t\t");
				for (int j = 0; j < semanticStack.size(); j++) { //语义栈
					bf.append(semanticStack.get(j));
				}
				deduce_str.remove(deduce_str.size() - 1);// 删掉匹配的字符，将产生式右边的字符压入堆栈
				if (right.equals("$")) {
					// 若产生式右边为空，只弹不压栈
				} else { //否则，压入分析栈
					String[] arg = right.split(" ");
					for (int i = arg.length - 1; i > -1; i--) {
						// 反向压栈
						deduce_str.add(arg[i]);
					}
				}
			}
			// 没有匹配的S-int型字符
			else {
				// input_cache.remove(0);
				// wordsList.remove(0);
				System.out.println(bf);
				System.out.println("语法分析失败\n");
				System.out.println("ERROR!  无法识别的字符" + input_cache.get(0) + "产生式" + leftandinput);
				break;

			}
		}
	}

	// 获取预测分析表
	public void getPredictMap() {
		String text_line;
		String left;
		String symbol;
		String right;
		try {
			// 初始化
			predictmap = new HashMap<String, String>();
			// 读取预测分析文件，以S@int -> func funcs为例
			File file = new File("predictldy.txt");
			RandomAccessFile predictfile = new RandomAccessFile(file, "r");
			while ((text_line = predictfile.readLine()) != null) {
				left = text_line.split("@")[0]; // S，非终结符
				symbol = (text_line.split("@")[1]).split("->")[0].trim(); // int，终结符
				// System.out.println(symbol);
				right = (text_line.split("@")[1]).split("->")[1].trim(); // func funcs
				predictmap.put(left + "-" + symbol, right); // S-int, func funcs

			}
			predictfile.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//记录中间运算T1，T2
	private String newTemp() {
		tempCount++;
		fourElemT.add("T" + tempCount);
		return "T" + tempCount;
	}

	/*
	 * 求四元式
	 * 动作符号
	 */
	private void actionSignOP(String symbol) {
		/**
		 * 分析栈栈顶单词是ADD_SUB 且操作符是+ - 弹栈 操作数2 操作数1 将保存结果的 压栈
		 */
		if (symbol.equals("ADD_SUB")) { // + - 做加减运算，产生四元式 (+,  a, b, T)
			if (OP != null && (OP.equals("+") || OP.equals("-"))) {
				ARG2 = semanticStack.pop();
				ARG1 = semanticStack.pop();
				RES = newTemp(); // 结果保存 T1 T2 T3
				FourElement fourElem = new FourElement(++fourElemCount, OP, ARG1, ARG2, RES);
				fourElemList.add(fourElem);//放入四元式列表中
				L = RES;//记录加减操作的结果
				semanticStack.push(L); // 将结果压栈
				OP = null;//操作符置空
			}
		} else if (symbol.equals("ADD")) { // ADD + 加标志
			OP = "+";
		} else if (symbol.equals("SUB")) { // SUB - 减标志
			OP = "-";
		} else if (symbol.equals("DIV_MUL")) { //做乘除运算，产生四元式  (*, a, b, T)
			if (OP != null && (OP.equals("*") || OP.equals("/"))) {
				ARG2 = semanticStack.pop();
				ARG1 = semanticStack.pop();
				RES = newTemp();
				FourElement fourElem = new FourElement(++fourElemCount, OP, ARG1, ARG2, RES);
				fourElemList.add(fourElem);
				T = RES;
				semanticStack.push(T);
				OP = null;
			}
		} else if (symbol.equals("DIV")) { //  /除号标志
			OP = "/";
		} else if (symbol.equals("MUL")) { //乘号标志
			OP = "*";
		} else if (symbol.equals("TRAN_LF")) { // L= T1 F=T1
			F = L; //记录加减操作的结果
			// semanticStack.push(F.value);

		} else if (symbol.equals("ASS_F")) {// ASS_F 标识符 压栈  IDN,NUM,CHAR,STR类型的标志
			F = firstWord.value; //F = 剩余符号串中的第一个元素
			if (!LexAnalyse.getTypelist().contains(F) && (F.charAt(0) > 64)) { //若F没有定义类型，则报错
				myError = new MyError(errorCount, "没有定义 " + F, firstWord.line, firstWord);
				myErrorList.add(myError);
				graErrorFlag = true; // 语义错误
			}
			semanticStack.push(F);

		} else if (symbol.equals("ASS_R")) { // ASS_F 标识符 压栈  IDN = xxx的标志，将IDN赋给R，判断R是否定义
			R = firstWord.value;
			if (!LexAnalyse.getTypelist().contains(R) && (R.charAt(0) > 64)) {
				myError = new MyError(errorCount, "没有定义 " + F, firstWord.line, firstWord);
				myErrorList.add(myError);
				graErrorFlag = true;
			}
			semanticStack.push(R);

		} else if (symbol.equals("ASS_Q")) { // ASS_Q 标识符 压栈   IDN ++或IDN--的标志
			Q = firstWord.value; //将IDN赋给Q
			semanticStack.push(Q);

		} else if (symbol.equals("SINGLE")) { // SINGLE for_op栈顶不为0  (++, i, /, i)
			if (for_op.peek() != null) {
				ARG1 = semanticStack.pop(); // 操作数1 弹栈
				RES = ARG1;
				FourElement fourElem = new FourElement(++fourElemCount, for_op.pop(), ARG1, "/", RES);
				fourElemList.add(fourElem);
			}

		} else if (symbol.equals("SINGLE_OP")) { // 单一操作符  若for里面有做i++或i--的操作，则将++或--放入for_op栈中
			for_op.push(firstWord.value);

		} else if (symbol.equals("EQ")) {  //等号的标志(=, a, /, b)
			OP = "=";
			ARG1 = semanticStack.pop();
			RES = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, ARG1, "/", RES);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("COMPARE")) { // (>=, a, b, T)
			ARG2 = semanticStack.pop();
			OP = semanticStack.pop();
			ARG1 = semanticStack.pop();
			RES = newTemp();
			FourElement fourElem = new FourElement(++fourElemCount, OP, ARG1, ARG2, RES);
			fourElemList.add(fourElem);
			G = RES;
			semanticStack.push(G);
			OP = null;

		} else if (symbol.equals("COMPARE_OP")) {// 比较符的标志，放入语义分析栈中
			D = firstWord.value;
			semanticStack.push(D);

		} else if (symbol.equals("IF_FJ")) { //if假跳转的标志 (FJ, a, T, /)
			OP = "FJ";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, RES, ARG1, "/");
			if_fj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("IF_BACKPATCH_FJ")) { //假跳转的回填标志
			backpatch(if_fj.pop(), fourElemCount + 2);

		} else if (symbol.equals("IF_RJ")) { //if真跳转标志(RJ, /, /, /)
			OP = "RJ";
			FourElement fourElem = new FourElement(++fourElemCount, OP, "/", "/", "/");
			if_rj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("IF_BACKPATCH_RJ")) { //if真跳转的回填标志
			backpatch(if_rj.pop(), fourElemCount + 1);

		} else if (symbol.equals("WHILE_FJ")) {  //while假跳转标志(FJ, /, T, /)
			OP = "FJ";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, "/", ARG1, "/");
			while_fj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("WHILE_RJ")) { //while真跳转标志
			OP = "RJ";
			RES = (while_fj.peek() - 1) + "";
			FourElement fourElem = new FourElement(++fourElemCount, OP, RES, "/", "/");
			for_rj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("WHILE_BACKPATCH_FJ")) {  //while假跳转的回填标志
			backpatch(while_fj.pop(), fourElemCount + 1);

		} else if (symbol.equals("FOR_FJ")) { //for假跳转标志
			OP = "FJ";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, "/", ARG1, "/");
			for_fj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("FOR_RJ")) { //for真跳转标志
			OP = "RJ";
			RES = (for_fj.peek() - 1) + "";
			FourElement fourElem = new FourElement(++fourElemCount, OP, RES, "/", "/");
			for_rj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("FOR_BACKPATCH_FJ")) { //for假跳转的回填标志
			backpatch(for_fj.pop(), fourElemCount + 1);
		}
		else if (symbol.equals("SCANF")) {
			OP = "SCANF";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, ARG1, "/", "/");
			// if_fj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;
			 
		} else if (symbol.equals("PRINTF")) { //(PRINTF, a, /, /)
			OP = "PRINTF";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, ARG1, "/", "/");
			fourElemList.add(fourElem);
			OP = null;
			 
		} 
	}

	// 回填操作
	private void backpatch(int i, int res) {
		FourElement temp = fourElemList.get(i - 1);
		temp.arg1 = res + "";
		fourElemList.set(i - 1, temp);

	}

	//输出四元式或错误信息
	private void outputInfo() {
		if (graErrorFlag) { // 语义出错
			bf.append("语义分析出错！\n");
			bf.append("错误信息如下：\n");
			bf.append("错误序号\t错误信息\t\t错误所在行 \t错误单词\n");
			for (int i = 0; i < myErrorList.size(); i++) {
				bf.append(myErrorList.get(i).id + "\t" + myErrorList.get(i).info + "\t\t" + myErrorList.get(i).line + "\t\t"
						+ myErrorList.get(i).word.value);
				bf.append("\n");
			}
		} else { // 语义正确 输出中间代码
			bf.append("语义分析成功！\n");
			bf.append("四元式结果如下:\n");
			bf.append("序号(操作,操作数1,操作数2,结果)\n");
			// System.out.println(bf);
			for (int i = 0; i < fourElemList.size(); i++) {
				bf.append(fourElemList.get(i).id + "(" + fourElemList.get(i).op + "," + fourElemList.get(i).arg1
						+ "," + fourElemList.get(i).arg2 + "," + fourElemList.get(i).result + ")");
				bf.append("\n");
			}
		}
		System.out.println(bf);
	}
	
	//输出四元式文件
		public String outputFourElem() throws IOException {
			FileOutputStream fos = new FileOutputStream("FourElement.txt");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw1 = new OutputStreamWriter(bos, "utf-8");
			PrintWriter pw1 = new PrintWriter(osw1);
			pw1.println("生成的四元式如下");
			pw1.println("序号（OP,ARG1，ARG2，RESULT）");
			FourElement temp;
			for (int i = 0; i < fourElemList.size(); i++) {
				temp = fourElemList.get(i);
				pw1.println(temp.id + "(" + temp.op + "," + temp.arg1 + "," + temp.arg2 + "," + temp.result + ")");
			}
			pw1.close();

			return "FourElement.txt";
		}

//	public static void main(String[] args) throws IOException {
//		LexAnalyse lex = new LexAnalyse();
//		lex.lexAnalyse1("test.txt");
//		lex.outputWordList();
//		AnalyseList analyse = new AnalyseList();
//
//		ArrayList<String> lex_result_stack = lex.get_Lex_Result();
//		TextParse textParse = new TextParse(lex_result_stack, lex);
//		textParse.Parsing();
//		textParse.outputFourElem();//输出四元式的文件
//	}

}