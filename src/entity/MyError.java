package entity;

/**
 * 自定义错误类
*/
public class MyError {
	public int id ;//错误序号；
	public String info;//错误信息；
	public int line ;//错误所在行
	public Words word;//错误的单词
	public MyError(){
		
	}

	public MyError(int id, String info, int line, Words word){
		this.id=id;
		this.info=info;
		this.line=line;
		this.word=word;
	}
}

