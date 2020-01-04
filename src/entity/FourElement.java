package entity;
/**
 * 四元式节点
 * @author Administrator
 *
 */
public class FourElement {

	public int id;//四元式序号，为编程方便
	public String op;//操作符
	public String arg1;//第一个操作数
	public String arg2;//第二个操作数
	public Object result;//结果
    public FourElement(){
	
    }

	public FourElement(int id,String op,String arg1,String arg2,String result){
		this.id=id;
		this.op=op;
		this.arg1=arg1;
		this.arg2=arg2;
		this.result=result;
   }
}
