package entity;

import java.util.ArrayList;

/*
 * �����࣬�жϵ�������
 * ������ű�ʱ����
*/
public class Words {
	public final static String KEY = "�ؼ���";
	public final static String OPERATOR = "�����";
	public final static String INT_CONST = "���ͳ���";
	public final static String CHAR_CONST = "�ַ�����";
	public final static String STRING_CONST = "�ַ�������";
	public final static String FLOAT_CONST = "���㳣��";
	public final static String BOOL_CONST = "��������";
	public final static String IDENTIFIER = "��־��";
	public final static String BOUNDARYSIGN = "���";
	public final static String END = "������";
	public final static String UNDEFI = "δ֪����";

	// �ؼ��ּ���
	public static ArrayList<String> key = new ArrayList<String>();

	// �������
	public static ArrayList<String> boundarySign = new ArrayList<String>();

	// ���������
	public static ArrayList<String> operator = new ArrayList<String>();

	// ��̬�����ʼ��
	static {
		Words.operator.add("+");
		Words.operator.add("-");
		Words.operator.add("++");
		Words.operator.add("--");
		Words.operator.add("*");
		Words.operator.add("/");
		Words.operator.add(">");
		Words.operator.add("<");
		Words.operator.add(">=");
		Words.operator.add("<=");
		Words.operator.add("==");
		Words.operator.add("!=");
		Words.operator.add("=");
		Words.operator.add("&&");
		Words.operator.add("||");
		Words.operator.add("!");
		Words.operator.add(".");
		Words.operator.add("?");
		Words.operator.add("|");
		Words.operator.add("&");
		Words.boundarySign.add("(");
		Words.boundarySign.add(")");
		Words.boundarySign.add("{");
		Words.boundarySign.add("}");
		Words.boundarySign.add(";");
		Words.boundarySign.add(",");
		Words.boundarySign.add("\\");
		Words.boundarySign.add("\'");
		Words.boundarySign.add("\"");
		Words.key.add("void");
		Words.key.add("main");
		Words.key.add("int");
		Words.key.add("char");
		Words.key.add("string");
		Words.key.add("float");
		Words.key.add("if");
		Words.key.add("else");
		Words.key.add("while");
		Words.key.add("for");
		Words.key.add("printf");
		Words.key.add("scanf");
		Words.key.add("break");
		Words.key.add("case");
		Words.key.add("continue");
		Words.key.add("do");
		Words.key.add("return");
		Words.key.add("static");
		Words.key.add("stdio.h");
		Words.key.add("include");
	}

	public int id;  // �������
	public String value;// ���ʵ�ֵ
	public String type;// ��������
	public String attribute;//���ʵ�����
	public int line;// ����������
	public boolean flag = true;//�����Ƿ�Ϸ�

	public Words() {

	}

	public Words(int id, String value, String type, int line) {
		this.id = id;
		this.value = value;
		this.type = type;
		this.line = line;
	}

	// �Ƿ���ڹؼ���
	public static boolean isKey(String word) {
		return key.contains(word);
	}

	// �Ƿ��������
	public static boolean isOperator(String word) {
		return operator.contains(word);
	}

	 // �ж��ǲ��ǽ��
	public static boolean isBoundarySign(String word) {
		return boundarySign.contains(word);
	}

	 //�жϵ����Ƿ�Ϊ���������
	public static boolean isArOP(String word) {
		if ((word.equals("+") || word.equals("-") || word.equals("*") || word
				.equals("/"))) {
			return true;
		} else {
			return false;
		}
	}

	 // �жϵ����Ƿ�Ϊ���������
	public static boolean isBoolOP(String word) {
		if ((word.equals(">") || word.equals("<") || word.equals("==")
				|| word.equals("!=") || word.equals("!") || word.equals("&&") || word
				.equals("||")))
			return true;
		else
			return false;
	}
}

