package entity;

import java.util.ArrayList;

/*
 * 单词类，判断单词属性
 * 构造符号表时可用
*/
public class Words {
	public final static String KEY = "关键字";
	public final static String OPERATOR = "运算符";
	public final static String INT_CONST = "整型常量";
	public final static String CHAR_CONST = "字符常量";
	public final static String STRING_CONST = "字符串常量";
	public final static String FLOAT_CONST = "浮点常量";
	public final static String BOOL_CONST = "布尔常量";
	public final static String IDENTIFIER = "标志符";
	public final static String BOUNDARYSIGN = "界符";
	public final static String END = "结束符";
	public final static String UNDEFI = "未知类型";

	// 关键字集合
	public static ArrayList<String> key = new ArrayList<String>();

	// 界符集合
	public static ArrayList<String> boundarySign = new ArrayList<String>();

	// 运算符集合
	public static ArrayList<String> operator = new ArrayList<String>();

	// 静态构造初始化
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

	public int id;  // 单词序号
	public String value;// 单词的值
	public String type;// 单词类型
	public String attribute;//单词的属性
	public int line;// 单词所在行
	public boolean flag = true;//单词是否合法

	public Words() {

	}

	public Words(int id, String value, String type, int line) {
		this.id = id;
		this.value = value;
		this.type = type;
		this.line = line;
	}

	// 是否存在关键字
	public static boolean isKey(String word) {
		return key.contains(word);
	}

	// 是否是运算符
	public static boolean isOperator(String word) {
		return operator.contains(word);
	}

	 // 判断是不是界符
	public static boolean isBoundarySign(String word) {
		return boundarySign.contains(word);
	}

	 //判断单词是否为算术运算符
	public static boolean isArOP(String word) {
		if ((word.equals("+") || word.equals("-") || word.equals("*") || word
				.equals("/"))) {
			return true;
		} else {
			return false;
		}
	}

	 // 判断单词是否为布尔运算符
	public static boolean isBoolOP(String word) {
		if ((word.equals(">") || word.equals("<") || word.equals("==")
				|| word.equals("!=") || word.equals("!") || word.equals("&&") || word
				.equals("||")))
			return true;
		else
			return false;
	}
}

