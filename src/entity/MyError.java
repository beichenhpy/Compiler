package entity;

/**
 * �Զ��������
*/
public class MyError {
	public int id ;//������ţ�
	public String info;//������Ϣ��
	public int line ;//����������
	public Words word;//����ĵ���
	public MyError(){
		
	}

	public MyError(int id, String info, int line, Words word){
		this.id=id;
		this.info=info;
		this.line=line;
		this.word=word;
	}
}

