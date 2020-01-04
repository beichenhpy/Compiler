package analyse;

import entity.Words;
import entity.MyError;
import entity.FourElement;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class TextParse {
	static ArrayList<String> actionSign = new ArrayList<String>();// ����������
	HashMap<String, String> predictmap; // Ԥ�������
	ArrayList<String> input_cache; // ����
	ArrayList<String> deduce_str; // ����ջ
	public ArrayList<MyError> myErrorList = new ArrayList<MyError>();
	int errorCount = 0;
	public ArrayList<FourElement> fourElemList = new ArrayList<FourElement>();// ��Ԫʽ
	public ArrayList<Words> wordsList = new ArrayList<Words>(); // ���ʱ�
	Words firstWord;// ����������
	public boolean graErrorFlag = false;// ��������־
	StringBuffer bf;// ����ջ������
	String OP = null; // ������
	String ARG1, ARG2, RES;
	int tempCount = 0;// ������ʱ����
	int fourElemCount = 0;// ��Ԫʽ���
	Stack<String> semanticStack = new Stack<String>();// ����ջ
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
	public final static String ACTIONSIGN = "������";
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
		getPredictMap(); //��ȡԤ�������
	}

	// �﷨����
	public void Parsing() {
		// �ķ���ʼ����ջ
		deduce_str.add("#");
		deduce_str.add("S");
		semanticStack.add("#"); // #������ջ
		String right;
		String leftandinput;
		bf = new StringBuffer();
		int index = 0;// �±�

		bf.append("����\t\t\t����ջ\t\t\t\t\t\t\t\tʣ�����봮\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t����ʽ\t\t\t\t\t\t\t����ջ");
		//�������Ĵ��������ַ������ַ�������ջ����Ϊ�գ���ʼ�﷨����
		while (deduce_str.size() > 0 && input_cache.size() > 0) {
			bf.append('\n');
			bf.append(index++ + "\t"); // ����

			firstWord = wordsList.get(0); // ����������
			// if(input_cache.get(0).equals(deduce_str.get(deduce_str.size()-1))&&input_cache.get(index))
			//��ʣ�����봮��ֻ��#ʱ���﷨�����ɹ�
			if (input_cache.get(0).equals(deduce_str.get(deduce_str.size() - 1)) && input_cache.get(0).equals("#")) {
				bf.append("\t\t�﷨�����ɹ�\n");
				//�����Ԫʽ�������Ϣ
				outputInfo();
				break;
			} else if (input_cache.get(0).equals(deduce_str.get(deduce_str.size() - 1))) {
				// ʣ�����봮��һ���ַ������ջ�е�һ���ַ���ȵĻ����������ʽ���Ƶ�����
				bf.append("\t\t"); // ����ջ
				for (int i = deduce_str.size() - 1; i > -1; i--) {
					bf.append(deduce_str.get(i));
				}
				bf.append("\t\t\t\t\t"); // ʣ�����봮
				for (int j = 0; j < wordsList.size(); j++) {
					bf.append(wordsList.get(j).value);
				}
				input_cache.remove(0);
				String pipei = deduce_str.remove(deduce_str.size() - 1); //ɾ��ƥ����ַ���������ʽ�ұߵ��ַ�ѹ���ջ
				wordsList.remove(0);//���ʱ�ջ����ջ
				bf.append("\t\t\t\t\t"); // ����ʽ
				bf.append(pipei + "ƥ��"); //ƥ����ַ�
				
				//����ջ
				bf.append("\t\t\t");
				for (int j = 0; j < semanticStack.size(); j++) {
					bf.append(semanticStack.get(j));
				}
				continue;
			}
			// ����S-int�͵��ַ�������Ԥ��������ƥ���ַ����ҵ���Ӧ�Ĳ���ʽ
			leftandinput = deduce_str.get(deduce_str.size() - 1) + "-" + input_cache.get(0); //
			// �ܹ��ҵ�ƥ���S-int���ַ�
			if ((right = predictmap.get(leftandinput)) != null) {
				//�������ջ�еĵ�һ��Ԫ�ص��ڶ�����ʱ����ʼ������Ԫʽ
				if (actionSign.contains(deduce_str.get(deduce_str.size() - 1)))
					actionSignOP(deduce_str.get(deduce_str.size() - 1));

				bf.append("\t\t"); // ����ջ
				for (int i = deduce_str.size() - 1; i > -1; i--) {
					bf.append(deduce_str.get(i));
				}
				bf.append("\t\t\t\t\t"); // ʣ�����봮
				for (int j = 0; j < wordsList.size(); j++) {
					bf.append(wordsList.get(j).value);
				}
				bf.append("\t\t\t\t\t"); // ����ʽ
				bf.append(deduce_str.get(deduce_str.size() - 1) + "->" + right);
				
				bf.append("\t\t\t");
				for (int j = 0; j < semanticStack.size(); j++) { //����ջ
					bf.append(semanticStack.get(j));
				}
				deduce_str.remove(deduce_str.size() - 1);// ɾ��ƥ����ַ���������ʽ�ұߵ��ַ�ѹ���ջ
				if (right.equals("$")) {
					// ������ʽ�ұ�Ϊ�գ�ֻ����ѹջ
				} else { //����ѹ�����ջ
					String[] arg = right.split(" ");
					for (int i = arg.length - 1; i > -1; i--) {
						// ����ѹջ
						deduce_str.add(arg[i]);
					}
				}
			}
			// û��ƥ���S-int���ַ�
			else {
				// input_cache.remove(0);
				// wordsList.remove(0);
				System.out.println(bf);
				System.out.println("�﷨����ʧ��\n");
				System.out.println("ERROR!  �޷�ʶ����ַ�" + input_cache.get(0) + "����ʽ" + leftandinput);
				break;

			}
		}
	}

	// ��ȡԤ�������
	public void getPredictMap() {
		String text_line;
		String left;
		String symbol;
		String right;
		try {
			// ��ʼ��
			predictmap = new HashMap<String, String>();
			// ��ȡԤ������ļ�����S@int -> func funcsΪ��
			File file = new File("predictldy.txt");
			RandomAccessFile predictfile = new RandomAccessFile(file, "r");
			while ((text_line = predictfile.readLine()) != null) {
				left = text_line.split("@")[0]; // S�����ս��
				symbol = (text_line.split("@")[1]).split("->")[0].trim(); // int���ս��
				// System.out.println(symbol);
				right = (text_line.split("@")[1]).split("->")[1].trim(); // func funcs
				predictmap.put(left + "-" + symbol, right); // S-int, func funcs

			}
			predictfile.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//��¼�м�����T1��T2
	private String newTemp() {
		tempCount++;
		fourElemT.add("T" + tempCount);
		return "T" + tempCount;
	}

	/*
	 * ����Ԫʽ
	 * ��������
	 */
	private void actionSignOP(String symbol) {
		/**
		 * ����ջջ��������ADD_SUB �Ҳ�������+ - ��ջ ������2 ������1 ���������� ѹջ
		 */
		if (symbol.equals("ADD_SUB")) { // + - ���Ӽ����㣬������Ԫʽ (+,  a, b, T)
			if (OP != null && (OP.equals("+") || OP.equals("-"))) {
				ARG2 = semanticStack.pop();
				ARG1 = semanticStack.pop();
				RES = newTemp(); // ������� T1 T2 T3
				FourElement fourElem = new FourElement(++fourElemCount, OP, ARG1, ARG2, RES);
				fourElemList.add(fourElem);//������Ԫʽ�б���
				L = RES;//��¼�Ӽ������Ľ��
				semanticStack.push(L); // �����ѹջ
				OP = null;//�������ÿ�
			}
		} else if (symbol.equals("ADD")) { // ADD + �ӱ�־
			OP = "+";
		} else if (symbol.equals("SUB")) { // SUB - ����־
			OP = "-";
		} else if (symbol.equals("DIV_MUL")) { //���˳����㣬������Ԫʽ  (*, a, b, T)
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
		} else if (symbol.equals("DIV")) { //  /���ű�־
			OP = "/";
		} else if (symbol.equals("MUL")) { //�˺ű�־
			OP = "*";
		} else if (symbol.equals("TRAN_LF")) { // L= T1 F=T1
			F = L; //��¼�Ӽ������Ľ��
			// semanticStack.push(F.value);

		} else if (symbol.equals("ASS_F")) {// ASS_F ��ʶ�� ѹջ  IDN,NUM,CHAR,STR���͵ı�־
			F = firstWord.value; //F = ʣ����Ŵ��еĵ�һ��Ԫ��
			if (!LexAnalyse.getTypelist().contains(F) && (F.charAt(0) > 64)) { //��Fû�ж������ͣ��򱨴�
				myError = new MyError(errorCount, "û�ж��� " + F, firstWord.line, firstWord);
				myErrorList.add(myError);
				graErrorFlag = true; // �������
			}
			semanticStack.push(F);

		} else if (symbol.equals("ASS_R")) { // ASS_F ��ʶ�� ѹջ  IDN = xxx�ı�־����IDN����R���ж�R�Ƿ���
			R = firstWord.value;
			if (!LexAnalyse.getTypelist().contains(R) && (R.charAt(0) > 64)) {
				myError = new MyError(errorCount, "û�ж��� " + F, firstWord.line, firstWord);
				myErrorList.add(myError);
				graErrorFlag = true;
			}
			semanticStack.push(R);

		} else if (symbol.equals("ASS_Q")) { // ASS_Q ��ʶ�� ѹջ   IDN ++��IDN--�ı�־
			Q = firstWord.value; //��IDN����Q
			semanticStack.push(Q);

		} else if (symbol.equals("SINGLE")) { // SINGLE for_opջ����Ϊ0  (++, i, /, i)
			if (for_op.peek() != null) {
				ARG1 = semanticStack.pop(); // ������1 ��ջ
				RES = ARG1;
				FourElement fourElem = new FourElement(++fourElemCount, for_op.pop(), ARG1, "/", RES);
				fourElemList.add(fourElem);
			}

		} else if (symbol.equals("SINGLE_OP")) { // ��һ������  ��for��������i++��i--�Ĳ�������++��--����for_opջ��
			for_op.push(firstWord.value);

		} else if (symbol.equals("EQ")) {  //�Ⱥŵı�־(=, a, /, b)
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

		} else if (symbol.equals("COMPARE_OP")) {// �ȽϷ��ı�־�������������ջ��
			D = firstWord.value;
			semanticStack.push(D);

		} else if (symbol.equals("IF_FJ")) { //if����ת�ı�־ (FJ, a, T, /)
			OP = "FJ";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, RES, ARG1, "/");
			if_fj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("IF_BACKPATCH_FJ")) { //����ת�Ļ����־
			backpatch(if_fj.pop(), fourElemCount + 2);

		} else if (symbol.equals("IF_RJ")) { //if����ת��־(RJ, /, /, /)
			OP = "RJ";
			FourElement fourElem = new FourElement(++fourElemCount, OP, "/", "/", "/");
			if_rj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("IF_BACKPATCH_RJ")) { //if����ת�Ļ����־
			backpatch(if_rj.pop(), fourElemCount + 1);

		} else if (symbol.equals("WHILE_FJ")) {  //while����ת��־(FJ, /, T, /)
			OP = "FJ";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, "/", ARG1, "/");
			while_fj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("WHILE_RJ")) { //while����ת��־
			OP = "RJ";
			RES = (while_fj.peek() - 1) + "";
			FourElement fourElem = new FourElement(++fourElemCount, OP, RES, "/", "/");
			for_rj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("WHILE_BACKPATCH_FJ")) {  //while����ת�Ļ����־
			backpatch(while_fj.pop(), fourElemCount + 1);

		} else if (symbol.equals("FOR_FJ")) { //for����ת��־
			OP = "FJ";
			ARG1 = semanticStack.pop();
			FourElement fourElem = new FourElement(++fourElemCount, OP, "/", ARG1, "/");
			for_fj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("FOR_RJ")) { //for����ת��־
			OP = "RJ";
			RES = (for_fj.peek() - 1) + "";
			FourElement fourElem = new FourElement(++fourElemCount, OP, RES, "/", "/");
			for_rj.push(fourElemCount);
			fourElemList.add(fourElem);
			OP = null;

		} else if (symbol.equals("FOR_BACKPATCH_FJ")) { //for����ת�Ļ����־
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

	// �������
	private void backpatch(int i, int res) {
		FourElement temp = fourElemList.get(i - 1);
		temp.arg1 = res + "";
		fourElemList.set(i - 1, temp);

	}

	//�����Ԫʽ�������Ϣ
	private void outputInfo() {
		if (graErrorFlag) { // �������
			bf.append("�����������\n");
			bf.append("������Ϣ���£�\n");
			bf.append("�������\t������Ϣ\t\t���������� \t���󵥴�\n");
			for (int i = 0; i < myErrorList.size(); i++) {
				bf.append(myErrorList.get(i).id + "\t" + myErrorList.get(i).info + "\t\t" + myErrorList.get(i).line + "\t\t"
						+ myErrorList.get(i).word.value);
				bf.append("\n");
			}
		} else { // ������ȷ ����м����
			bf.append("��������ɹ���\n");
			bf.append("��Ԫʽ�������:\n");
			bf.append("���(����,������1,������2,���)\n");
			// System.out.println(bf);
			for (int i = 0; i < fourElemList.size(); i++) {
				bf.append(fourElemList.get(i).id + "(" + fourElemList.get(i).op + "," + fourElemList.get(i).arg1
						+ "," + fourElemList.get(i).arg2 + "," + fourElemList.get(i).result + ")");
				bf.append("\n");
			}
		}
		System.out.println(bf);
	}
	
	//�����Ԫʽ�ļ�
		public String outputFourElem() throws IOException {
			FileOutputStream fos = new FileOutputStream("FourElement.txt");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw1 = new OutputStreamWriter(bos, "utf-8");
			PrintWriter pw1 = new PrintWriter(osw1);
			pw1.println("���ɵ���Ԫʽ����");
			pw1.println("��ţ�OP,ARG1��ARG2��RESULT��");
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
//		textParse.outputFourElem();//�����Ԫʽ���ļ�
//	}

}